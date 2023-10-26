package dev.tryharddo.sentry.creatures;

import dev.tryharddo.sentry.settings.SentryDescriptor;
import dev.tryharddo.sentry.utils.MathUtils;
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
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntitySentry extends ArmorStand implements RangedAttackMob {
    private final SentryDescriptor sentryDescriptor;
    private boolean isDisabled = false;
    private int ammoMagazineCounter;
    private static final int memoryThreshold = 200;
    private int lastShootTicks = 0;
    private int idleTicks = 0;
    private UUID memorizedEnemyUUID = null;
    private int lastSeenMemorizedEnemyTicks = 0;

    public EntitySentry(World world, @NotNull Location location, UUID owner) {
        super(EntityType.ARMOR_STAND, ((CraftWorld) world).getHandle());
        this.sentryDescriptor = new SentryDescriptor(owner);
        this.setPos(location.getX(), location.getY(), location.getZ());

        this.ammoMagazineCounter = this.sentryDescriptor.getAmmoBagSize();
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

        Collection<org.bukkit.entity.LivingEntity> nearbyLivingEntities = world.getNearbyEntities(boundingBox).stream()
                .filter(entity -> entity instanceof org.bukkit.entity.LivingEntity)
                .map(entity -> (org.bukkit.entity.LivingEntity) entity)
                .collect(Collectors.toSet());

        if (nearbyLivingEntities.isEmpty()) return;

        nearbyLivingEntities.removeIf(le -> {
            if (!le.isValid()) return true;
            if (le.getUniqueId().equals(this.uuid)) return true;
            if (this.sentryDescriptor.getSentryOwners().contains(le.getUniqueId())) return true;
            if (this.sentryDescriptor.getMobWhiteList().contains(le.getType())) return true;
            return !this.hasLineOfSight(((CraftEntity) le).getHandle());
        });

        org.bukkit.entity.LivingEntity attackTarget = null;
        for (org.bukkit.entity.LivingEntity livingEntity : nearbyLivingEntities) {
            if (livingEntity.getUniqueId().equals(this.memorizedEnemyUUID) &&
            this.lastSeenMemorizedEnemyTicks < memoryThreshold) {
                attackTarget = livingEntity;
                break;
            }
        }

        if (attackTarget == null) {
            ++this.lastSeenMemorizedEnemyTicks;
            if (this.lastSeenMemorizedEnemyTicks > memoryThreshold) {
                this.resetMemory();
            }

            attackTarget = this.getClosestLivingEntity(nearbyLivingEntities);
        }

        if (attackTarget == null) return;

        Location ceLoc = attackTarget.getEyeLocation();
        this.setHeadTargeting(ceLoc.getX(), ceLoc.getY(), ceLoc.getZ());

        if (this.ammoMagazineCounter == 0) {
            ++this.idleTicks;
            if (this.idleTicks == this.sentryDescriptor.getReloadTickSpeed()) {
                this.ammoMagazineCounter = this.sentryDescriptor.getMagazineSize();
                this.idleTicks = 0;
                this.lastShootTicks = 0;

                this.playSound(SoundEvents.PISTON_EXTEND, 1.0f, 1.5f);
            }

            return;
        }

        ++this.lastShootTicks;
        if (this.lastShootTicks % this.sentryDescriptor.getAttackSpeed() == 0) {
            this.performRangedAttack(((CraftLivingEntity) attackTarget).getHandle(), 1.0F);

            if (this.memorizedEnemyUUID == null) {
                this.memorizedEnemyUUID = attackTarget.getUniqueId();
            }

            this.ammoMagazineCounter--;
            this.lastShootTicks = 0;
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

    private void resetMemory() {
        this.lastSeenMemorizedEnemyTicks = 0;
        this.memorizedEnemyUUID = null;
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

            this.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    private @Nullable org.bukkit.entity.LivingEntity getClosestLivingEntity(@NotNull Collection<org.bukkit.entity.LivingEntity> entities) {
        double closestDistance = Double.MAX_VALUE;
        org.bukkit.entity.LivingEntity closestEnt = null;

        for (org.bukkit.entity.LivingEntity ent : entities) {
            Location bukkitLoc = new Location(this.level().getWorld(), this.getX(), this.getY(), this.getZ());
            double distance = bukkitLoc.distance(ent.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnt = ent;
            }
        }

        return closestEnt;
    }

    private void setHeadTargeting(double x, double y, double z) {
        Vec3 posVec = this.position();
        double pitch = MathUtils.computePitch(posVec.x(), posVec.y(), posVec.z(), x, y, z);
        double yaw = MathUtils.computeYaw(posVec.x(), posVec.z(), x, z);

        this.setHeadPose(new Rotations((float)Math.toDegrees(pitch), 0, 0));
        this.setYHeadRot((float)Math.toDegrees(yaw));
        this.setYRot((float)Math.toDegrees(yaw));
        this.yRotO = (float)Math.toDegrees(yaw);
    }

    protected AbstractArrow getArrow(ItemStack itemstack, float f) {
        return ProjectileUtil.getMobArrow(this, itemstack, f);
    }
}
