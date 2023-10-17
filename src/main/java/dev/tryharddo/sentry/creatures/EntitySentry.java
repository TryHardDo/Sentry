package dev.tryharddo.sentry.creatures;

import dev.tryharddo.sentry.Sentry;
import dev.tryharddo.sentry.settings.SentryDescriptor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EntitySentry implements InventoryHolder {
    private static final String SENTRY_ID_DATA = "SentryId";
    private static final String SENTRY_INVENTORY_DATA = "SentryInvId";
    private static final HashMap<Class<? extends Projectile>, Material> validAmmoMap = new HashMap<>();
    private final SentryDescriptor descriptor;
    private final ArmorStand sentryBody;
    private final Inventory sentryInventory;
    private boolean isDisabled = false;
    private int tickCounter = 0;

    public EntitySentry(Location location, UUID owner) {
        this.descriptor = new SentryDescriptor(owner);
        this.sentryBody = this.constructBody(location);

        if (this.sentryBody == null) {
            throw new IllegalStateException("Sentry's body is null! Can't spawn sentry body!");
        }

        this.sentryInventory = this.constructInventory();
    }

    public EntitySentry(Location location, SentryDescriptor sentryDescriptor) {
        this.descriptor = sentryDescriptor;
        this.sentryBody = constructBody(location);

        if (this.sentryBody == null) {
            throw new IllegalStateException("Sentry's body is null! Can't spawn sentry body!");
        }

        this.sentryInventory = this.constructInventory();
    }

    public void tick() {
        if (isDisabled) {
            return;
        }

        tickCounter++;

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

            return !hasLineOfSight(sentryBody.getEyeLocation(), le.getEyeLocation());
        });

        LivingEntity closestEntity = getClosestEntity(sentryBody.getLocation(), livingEntities);

        if (closestEntity == null) {
            return;
        }

        setHeadTargeting(sentryBody.getEyeLocation(), closestEntity.getEyeLocation());

        if (tickCounter % descriptor.getAttackSpeed() == 0) {
            Vector shootingVec = getShootingVector(sentryBody.getEyeLocation(), closestEntity.getEyeLocation());
            launchProjectile(sentryBody, shootingVec);

            sentryWorld.playSound(sentryBody.getLocation(), descriptor.getAttackSound(), 1, 1);
        }
    }

    private void setHeadTargeting(@NotNull Location source, @NotNull Location target) {
        double pitch = calculatePitch(source, target);
        double yaw = calculateYaw(source, target);

        sentryBody.setHeadPose(new EulerAngle(pitch, 0, 0));
        sentryBody.setRotation((float) Math.toDegrees(yaw), 0);
    }

    @Contract("_, _ -> !null")
    private @NotNull Projectile launchProjectile(@NotNull ProjectileSource source, Vector vector) {
        return source.launchProjectile(descriptor.getAmmoType(), vector);
    }

    private double calculatePitch(@NotNull Location sourceLocation, @NotNull Location targetLocation) {
        double dx = targetLocation.getX() - sourceLocation.getX();
        double dy = targetLocation.getY() - sourceLocation.getY();
        double dz = targetLocation.getZ() - sourceLocation.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return -Math.asin(dy / distance);
    }

    private double calculateYaw(@NotNull Location sourceLocation, @NotNull Location targetLocation) {
        double dx = targetLocation.getX() - sourceLocation.getX();
        double dz = targetLocation.getZ() - sourceLocation.getZ();
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        double yaw = Math.atan2(dx / distanceXZ, dz / distanceXZ);
        if (yaw < 0) {
            yaw += 2 * Math.PI;
        }
        return -yaw;
    }

    private @NotNull Vector getShootingVector(@NotNull Location from, @NotNull Location to, float speed) {
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

    private @NotNull Vector getShootingVector(@NotNull Location from, @NotNull Location to) {
        return getShootingVector(from, to, 1.6f);
    }

    private @NotNull Inventory constructInventory() {
        return Bukkit.createInventory(this, this.descriptor.getAmmoBagSize());
    }

    private @Nullable LivingEntity getClosestEntity(Location location, @NotNull List<LivingEntity> mobs) {
        double closestDistance = Double.MAX_VALUE;
        LivingEntity closestEnt = null;

        for (LivingEntity ent : mobs) {
            double distance = location.distance(ent.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnt = ent;
            }
        }

        return closestEnt;
    }

    private @NotNull List<LivingEntity> filterLivingEntities(@NotNull Collection<Entity> entities) {
        List<LivingEntity> buffer = new ArrayList<>();

        for (Entity e : entities) {
            if (e instanceof LivingEntity) {
                buffer.add(((LivingEntity) e));
            }
        }

        return buffer;
    }

    private @Nullable ArmorStand constructBody(@NotNull Location location) {
        World world = location.getWorld();

        if (world == null) {
            return null;
        }

        ArmorStand as = ((ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND));
        as.setBasePlate(false);
        as.setCustomName(this.descriptor.getDisplayName());
        as.setCustomNameVisible(this.descriptor.isShowDisplayName());
        as.setRemoveWhenFarAway(false);

        PersistentDataContainer bodyContainer = as.getPersistentDataContainer();
        bodyContainer.set(new NamespacedKey(Sentry.getInstance(), SENTRY_ID_DATA), PersistentDataType.STRING, this.descriptor.getSentryId().toString());

        return as;
    }

    private boolean hasLineOfSight(@NotNull Location from, @NotNull Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        World fromWorld = from.getWorld();
        World toWorld = to.getWorld();

        if (fromWorld == null || toWorld == null) {
            throw new NullPointerException("Both world (to) and (from) must not be null!");
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
}