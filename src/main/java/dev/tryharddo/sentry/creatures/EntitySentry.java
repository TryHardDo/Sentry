package dev.tryharddo.sentry.creatures;

import dev.tryharddo.sentry.settings.SentryDescriptor;
import net.minecraft.core.Rotations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class EntitySentry extends ArmorStand implements RangedAttackMob {
    private final SentryDescriptor sentryDescriptor;
    private boolean isDisabled = false;
    private int ammoMagazineCounter;
    private int lastShootTicks = 0;
    private int idleTicks = 0;

    public EntitySentry(World world, @NotNull Location location, UUID owner) {
        super(EntityType.ARMOR_STAND, ((CraftWorld) world).getHandle());
        this.sentryDescriptor = new SentryDescriptor(owner);
        this.setPos(location.getX(), location.getY(), location.getZ());

        this.ammoMagazineCounter = this.sentryDescriptor.getAmmoBagSize();
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

    @Override
    public void tick() {
        super.tick();

        if (isDisabled) return;

        if (!this.valid || !this.isAlive() || this.isRemoved() || !this.isChunkLoaded()) return;

        int attackRange = sentryDescriptor.getAttackRadius();
        CraftWorld world = this.level().getWorld();
        Location bukkitLoc = new Location(world, this.getX(), this.getY(), this.getZ());
        BoundingBox boundingBox = BoundingBox.of(bukkitLoc, attackRange, attackRange, attackRange);

        Collection<Entity> nearbyEnt = world.getNearbyEntities(boundingBox);

        if (nearbyEnt.isEmpty()) return;

        Collection<Entity> goodEntColl = new HashSet<>();

        for (Entity e : nearbyEnt) {
            if (!e.isValid()) continue;
            if (e.getUniqueId().equals(this.uuid)) continue;
            if (sentryDescriptor.getSentryOwners().contains(e.getUniqueId())) continue;
            if (sentryDescriptor.getMobWhiteList().contains(e.getType())) continue;

            // Line of sight
            goodEntColl.add(e);
        }

        org.bukkit.entity.LivingEntity closestEnt = this.getClosestLivingEntity(goodEntColl);

        if (closestEnt == null) return;

        if (ammoMagazineCounter == 0) {
            if (++idleTicks == sentryDescriptor.getReloadTickSpeed()) {
                ammoMagazineCounter = sentryDescriptor.getMagazineSize();
                idleTicks = 0;
                // Sound play
            }

            return;
        }

        if (++lastShootTicks % sentryDescriptor.getAttackSpeed() == 0) {
            this.performRangedAttack(((CraftLivingEntity) closestEnt).getHandle(), 1.0F);
            ammoMagazineCounter--;
            lastShootTicks = 0;
        }
    }

    // Getters & Setters
    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    private int getMagazineSize() {
        return this.sentryDescriptor.getMagazineSize();
    }

    public int getAmmoMagazineCount() {
        return this.ammoMagazineCounter;
    }

    public void setAmmoMagazineCount(int magazineCount) {
        this.ammoMagazineCounter = magazineCount;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float v) {
        ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrow entArrow = this.getArrow(itemstack, v);
        double d0 = livingEntity.getX() - this.getX();
        double d1 = livingEntity.getY(0.3333333333333333) - entArrow.getY();
        double d2 = livingEntity.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        entArrow.shoot(d0, d1 + d3 * 0.20000000298023224, d2, 1.6F, (float) (14 - this.level().getDifficulty().getId() * 4));
        EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(this, this.getMainHandItem(), null, entArrow, InteractionHand.MAIN_HAND, 0.8F, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
        } else {
            if (event.getProjectile() == entArrow.getBukkitEntity()) {
                this.level().addFreshEntity(entArrow);
            }

            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    private @Nullable org.bukkit.entity.LivingEntity getClosestLivingEntity(@NotNull Collection<org.bukkit.entity.Entity> entities) {
        double closestDistance = Double.MAX_VALUE;
        org.bukkit.entity.LivingEntity closestEnt = null;

        for (org.bukkit.entity.Entity ent : entities) {
            if (!(ent instanceof org.bukkit.entity.LivingEntity)) continue;

            org.bukkit.entity.LivingEntity le = ((org.bukkit.entity.LivingEntity) ent);
            Location bukkitLoc = new Location(this.level().getWorld(), this.getX(), this.getY(), this.getZ());
            double distance = bukkitLoc.distance(ent.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnt = le;
            }
        }

        return closestEnt;
    }

    private void setHeadTargeting(@NotNull Location target) {
        Location source = this.getBukkitEntity().getLocation();

        double pitch = calculatePitch(source, target);
        double yaw = calculateYaw(source, target);

        this.headPose = new Rotations((float) pitch, 0, 0);
    }

    protected AbstractArrow getArrow(ItemStack itemstack, float f) {
        return ProjectileUtil.getMobArrow(this, itemstack, f);
    }
}
