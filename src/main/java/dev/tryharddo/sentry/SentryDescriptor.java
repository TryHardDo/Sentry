package dev.tryharddo.sentry;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.UUID;

public class SentryDescriptor {
    private UUID sentryId = UUID.randomUUID();
    private HashSet<UUID> sentryOwners = new HashSet<>();
    private HashSet<EntityType> mobWhiteList = new HashSet<>();
    private HashSet<UUID> playerWhiteList = new HashSet<>();
    private int attackRadius = 5;
    private double damageMultiplier = 1.0;
    private String displayName = "Sentry";
    private boolean showDisplayName = true;
    private EntityType ammoType = EntityType.ARROW;
    private double maxHealth = 20.0;
    private Sound attackSound = Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;

    public SentryDescriptor() {}

    public SentryDescriptor(UUID sentryId) {
        this.sentryId = sentryId;
    }

    /**
     * Adds an entityType to the whitelist set.
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
}
