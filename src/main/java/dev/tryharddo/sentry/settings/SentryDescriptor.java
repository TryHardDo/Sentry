package dev.tryharddo.sentry.settings;

import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class SentryDescriptor {
    private UUID sentryId = UUID.randomUUID();
    private HashSet<UUID> sentryOwners = new HashSet<>();
    private HashSet<EntityType> mobWhiteList = new HashSet<>();
    private HashSet<UUID> playerWhiteList = new HashSet<>();
    private int attackRadius = 5;
    private int ammoBagSize = 9;
    private double damageMultiplier = 1.0;
    private String displayName = "Sentry";
    private boolean showDisplayName = true;
    private double maxHealth = 20.0;
    private Sound attackSound = Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;
    private int attackSpeed = 8;
    private Class<? extends Projectile> ammoType = LlamaSpit.class;
    private int reloadTickSpeed = 60;
    private int magazineSize = 25;

    public SentryDescriptor(UUID owner) {
        this.addOwner(owner);
    }

    public SentryDescriptor(UUID owner, UUID sentryId) {
        this.addOwner(owner);
        this.sentryId = sentryId;
    }

    /**
     * Adds an entityType to the whitelist set.
     *
     * @param entityType The entity type of the mob.
     * @return true if the whitelist set did not already contain the specified element
     */
    public boolean addMobToWhitelist(EntityType entityType) {
        return mobWhiteList.add(entityType);
    }

    public boolean addMobToWhitelist(@NotNull Entity entity) {
        return mobWhiteList.add(entity.getType());
    }

    public boolean removeMobFromWhiteList(EntityType entityType) {
        return mobWhiteList.remove(entityType);
    }

    public boolean removeMobFromWhiteList(@NotNull Entity entity) {
        return mobWhiteList.remove(entity.getType());
    }

    public boolean addPlayerToWhiteList(@NotNull Player player) {
        return playerWhiteList.add(player.getUniqueId());
    }

    public boolean addPlayerToWhiteList(UUID uuid) {
        return playerWhiteList.add(uuid);
    }

    public boolean removePlayerFromWhiteList(@NotNull Player player) {
        return playerWhiteList.remove(player.getUniqueId());
    }

    public boolean removePlayerFromWhiteList(UUID uuid) {
        return playerWhiteList.remove(uuid);
    }

    public boolean addOwner(@NotNull Player player) {
        return sentryOwners.add(player.getUniqueId());
    }

    public boolean addOwner(UUID uuid) {
        return sentryOwners.add(uuid);
    }

    public boolean removeOwner(@NotNull Player player) {
        return sentryOwners.remove(player.getUniqueId());
    }

    public boolean removeOwner(UUID uuid) {
        return sentryOwners.remove(uuid);
    }

    public UUID getSentryId() {
        return sentryId;
    }

    public HashSet<UUID> getSentryOwners() {
        return sentryOwners;
    }

    public void setSentryOwners(HashSet<UUID> sentryOwners) {
        this.sentryOwners = sentryOwners;
    }

    public HashSet<EntityType> getMobWhiteList() {
        return mobWhiteList;
    }

    public void setMobWhiteList(HashSet<EntityType> mobWhiteList) {
        this.mobWhiteList = mobWhiteList;
    }

    public HashSet<UUID> getPlayerWhiteList() {
        return playerWhiteList;
    }

    public void setPlayerWhiteList(HashSet<UUID> playerWhiteList) {
        this.playerWhiteList = playerWhiteList;
    }

    public int getAttackRadius() {
        return attackRadius;
    }

    public void setAttackRadius(int attackRadius) {
        this.attackRadius = attackRadius;
    }

    public int getAmmoBagSize() {
        return ammoBagSize;
    }

    public void setAmmoBagSize(int ammoBagSize) {
        this.ammoBagSize = ammoBagSize;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isShowDisplayName() {
        return showDisplayName;
    }

    public void setShowDisplayName(boolean showDisplayName) {
        this.showDisplayName = showDisplayName;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Sound getAttackSound() {
        return attackSound;
    }

    public void setAttackSound(Sound attackSound) {
        this.attackSound = attackSound;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(int attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public Class<? extends Projectile> getAmmoType() {
        return ammoType;
    }

    public void setAmmoType(Class<? extends Projectile> ammoType) {
        this.ammoType = ammoType;
    }

    public int getReloadTickSpeed() {
        return reloadTickSpeed;
    }

    public void setReloadTickSpeed(int reloadTickSpeed) {
        this.reloadTickSpeed = reloadTickSpeed;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public void setMagazineSize(int magazineSize) {
        this.magazineSize = magazineSize;
    }
}
