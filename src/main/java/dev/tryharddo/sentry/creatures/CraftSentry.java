package dev.tryharddo.sentry.creatures;

import dev.tryharddo.sentry.events.SentryProjectileLaunchEvent;
import dev.tryharddo.sentry.events.SentryTargetingEvent;
import dev.tryharddo.sentry.settings.SentryDescriptor;
import net.minecraft.world.level.Level;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CraftSentry implements InventoryHolder {
    public static final NamespacedKey AMMO_MARKER_DATA;
    public static final NamespacedKey SENTRY_ID_HOLDER_DATA;
    public static final NamespacedKey SENTRY_INVENTORY_CONTENT_DATA;

    static {
        AMMO_MARKER_DATA = NamespacedKey.minecraft("sentry_ammo_marker_data");
        SENTRY_ID_HOLDER_DATA = NamespacedKey.minecraft("sentry_id_data");
        SENTRY_INVENTORY_CONTENT_DATA = NamespacedKey.minecraft("sentry_inventory_data");
    }

    private final SentryDescriptor descriptor;
    private final ArmorStand sentryBody;
    private final Inventory sentryInventory;
    private boolean isDisabled = false;
    private int ticksFromLastShoot = 0;
    private int idleTickCounter = 0;
    private int shootLeftBeforeReload;

    public CraftSentry(Location location, UUID owner) {
        this.descriptor = new SentryDescriptor(owner);
        this.shootLeftBeforeReload = descriptor.getMagazineSize();
        this.sentryBody = this.constructBody(location);

        if (this.sentryBody == null) {
            throw new IllegalStateException("Sentry's body is null! Can't spawn sentry body!");
        }

        this.sentryInventory = this.constructInventory();
    }

    public CraftSentry(Location location, SentryDescriptor sentryDescriptor) {
        this.descriptor = sentryDescriptor;
        this.shootLeftBeforeReload = descriptor.getMagazineSize();
        this.sentryBody = constructBody(location);

        if (this.sentryBody == null) {
            throw new IllegalStateException("Sentry's body is null! Can't spawn sentry body!");
        }

        this.sentryInventory = this.constructInventory();
    }

    private static double calculatePitch(@NotNull Location sourceLocation, @NotNull Location targetLocation) {
        double dx = targetLocation.getX() - sourceLocation.getX();
        double dy = targetLocation.getY() - sourceLocation.getY();
        double dz = targetLocation.getZ() - sourceLocation.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return -Math.asin(dy / distance);
    }

    private static double calculateYaw(@NotNull Location sourceLocation, @NotNull Location targetLocation) {
        double dx = targetLocation.getX() - sourceLocation.getX();
        double dz = targetLocation.getZ() - sourceLocation.getZ();
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        double yaw = Math.atan2(dx / distanceXZ, dz / distanceXZ);
        if (yaw < 0) {
            yaw += 2 * Math.PI;
        }
        return -yaw;
    }

    private static @NotNull Vector getShootingVector(@NotNull Location from, @NotNull Location to, float speed) {
        double d0 = to.getY() - 1.100000023841858;
        double d1 = to.getX() - from.getX();
        double d2 = d0 - from.getY();
        double d3 = to.getZ() - from.getZ();
        double d4 = Math.sqrt(d1 * d1 + d3 * d3) * 0.20000000298023224;

        Vector velocity = new Vector(d1, d2 + d4, d3);
        velocity.normalize();
        velocity.multiply(speed);

        return velocity;
    }

    private static @NotNull Vector getShootingVector(@NotNull Location from, @NotNull Location to) {
        return getShootingVector(from, to, 1.6f);
    }

    private static @NotNull List<LivingEntity> filterLivingEntities(@NotNull Collection<Entity> entities) {
        List<LivingEntity> buffer = new ArrayList<>();

        for (Entity e : entities) {
            if (e instanceof LivingEntity) {
                buffer.add(((LivingEntity) e));
            }
        }

        return buffer;
    }

    public void tick() {
        if (isDisabled) {
            return;
        }

        if (!sentryBody.isValid()) {
            return;
        }

        if (!sentryBody.getWorld().isChunkLoaded(sentryBody.getLocation().getChunk())) {
            return;
        }

        World sentryWorld = sentryBody.getWorld();
        int attackRange = descriptor.getAttackRadius();
        Collection<Entity> nearbyEntities = sentryWorld.getNearbyEntities(sentryBody.getLocation(), attackRange, attackRange, attackRange);

        if (nearbyEntities.isEmpty()) {
            return;
        }

        List<LivingEntity> livingEntities = filterLivingEntities(nearbyEntities);

        if (livingEntities.isEmpty()) {
            return;
        }

        livingEntities.removeIf(le -> {
            if (!le.isValid()) {
                return true;
            }

            if (le.getUniqueId().equals(sentryBody.getUniqueId())) {
                return true;
            }

            if (descriptor.getSentryOwners().contains(le.getUniqueId())) {
                return true;
            }

            if (descriptor.getMobWhiteList().contains(le.getType())) {
                return true;
            }

            return !hasLineOfSight(le.getEyeLocation());
        });

        LivingEntity closestEntity = getClosestEntity(livingEntities);

        if (closestEntity == null) {
            return;
        }

        SentryTargetingEvent targetingEvent = new SentryTargetingEvent(this, closestEntity, EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        Bukkit.getPluginManager().callEvent(targetingEvent);

        if (targetingEvent.isCancelled()) {
            return;
        }

        setHeadTargeting(closestEntity.getEyeLocation());

        if (shootLeftBeforeReload == 0) {
            if (++idleTickCounter == descriptor.getReloadTickSpeed()) {
                shootLeftBeforeReload = descriptor.getMagazineSize();
                idleTickCounter = 0;
                sentryBody.getWorld().playSound(sentryBody.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1, 2);
            }
            return;
        }

        if (++ticksFromLastShoot % descriptor.getAttackSpeed() == 0) {
            Projectile projectile = launchProjectile(closestEntity, descriptor.getAmmoType());

            if (projectile == null) {
                return;
            }

            sentryWorld.playSound(sentryBody.getLocation(), descriptor.getAttackSound(), 1, 1);
            shootLeftBeforeReload--;
            ticksFromLastShoot = 0;
        }
    }

    private void setHeadTargeting(@NotNull Location target) {
        Location source = sentryBody.getEyeLocation();

        double pitch = calculatePitch(source, target);
        double yaw = calculateYaw(source, target);

        sentryBody.setHeadPose(new EulerAngle(pitch, 0, 0));
        sentryBody.setRotation((float) Math.toDegrees(yaw), 0);
    }

    private @Nullable Projectile launchProjectile(@NotNull LivingEntity target, Class<? extends Projectile> projectile) {
        Level level = ((CraftWorld) target.getWorld()).getHandle();
        net.minecraft.world.entity.Entity projEntity = new net.minecraft.world.entity.projectile.Arrow(level, ((CraftLivingEntity) sentryBody).getHandle());

        Projectile proj = (Projectile) (projEntity.getBukkitEntity());
        Vector shootingVec = getShootingVector(sentryBody.getEyeLocation(), target.getEyeLocation());
        proj.setVelocity(shootingVec);

        PersistentDataContainer dataContainer = proj.getPersistentDataContainer();
        dataContainer.set(AMMO_MARKER_DATA, PersistentDataType.BYTE, (byte) 1);

        SentryProjectileLaunchEvent launchEvent = new SentryProjectileLaunchEvent(proj, this);
        Bukkit.getPluginManager().callEvent(launchEvent);

        if (launchEvent.isCancelled()) {
            return null;
        }

        level.addFreshEntity(projEntity);

        return proj;
    }

    private @NotNull Inventory constructInventory() {
        return Bukkit.createInventory(this, this.descriptor.getAmmoBagSize());
    }

    private @Nullable LivingEntity getClosestEntity(@NotNull List<LivingEntity> mobs) {
        double closestDistance = Double.MAX_VALUE;
        LivingEntity closestEnt = null;

        for (LivingEntity ent : mobs) {
            double distance = sentryBody.getLocation().distance(ent.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnt = ent;
            }
        }

        return closestEnt;
    }

    private @Nullable ArmorStand constructBody(@NotNull Location location) {
        World world = location.getWorld();

        if (world == null) {
            return null;
        }

        ArmorStand as = ((ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND));
        PersistentDataContainer asDataContainer = as.getPersistentDataContainer();
        asDataContainer.set(SENTRY_ID_HOLDER_DATA, PersistentDataType.STRING, descriptor.getSentryId().toString());

        as.setBasePlate(false);
        as.setCustomName(this.descriptor.getDisplayName());
        as.setCustomNameVisible(this.descriptor.isShowDisplayName());
        as.setArms(true);
        as.setRemoveWhenFarAway(false);

        return as;
    }

    private boolean hasLineOfSight(@NotNull Location target) {
        Location from = sentryBody.getEyeLocation();
        Vector direction = target.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        World fromWorld = from.getWorld();
        World toWorld = target.getWorld();

        if (fromWorld == null || toWorld == null) {
            throw new NullPointerException("Both world (target) and (from) must not be null!");
        }

        if (!fromWorld.getUID().equals(toWorld.getUID())) {
            throw new IllegalStateException("The ray trace can't be computed in different worlds.");
        }

        return fromWorld.rayTraceBlocks(from, direction, distance, FluidCollisionMode.NEVER, true) == null;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.sentryInventory;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public boolean isValid() {
        return sentryBody.isValid();
    }

    public SentryDescriptor getDescriptor() {
        return descriptor;
    }

    public ArmorStand getSentryBody() {
        return this.sentryBody;
    }
}