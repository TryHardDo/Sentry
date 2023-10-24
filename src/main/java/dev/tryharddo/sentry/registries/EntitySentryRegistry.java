package dev.tryharddo.sentry.registries;

import dev.tryharddo.sentry.creatures.CraftSentry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class EntitySentryRegistry {
    private final HashMap<UUID, CraftSentry> registeredSentries;

    public EntitySentryRegistry(HashMap<UUID, CraftSentry> sentries) {
        registeredSentries = sentries;
    }

    public EntitySentryRegistry() {
        registeredSentries = new HashMap<>();
    }

    /**
     * Registers an CraftSentry in the sentry registry.
     *
     * @param craftSentry the sentry entity
     * @return null if no swap happened or the previous value was null
     * or the swapped CraftSentry object.
     */
    public @Nullable CraftSentry registerSentry(@NotNull CraftSentry craftSentry) {
        return registeredSentries.put(craftSentry.getDescriptor().getSentryId(), craftSentry);
    }

    public CraftSentry unregisterSentry(@NotNull CraftSentry craftSentry) {
        return registeredSentries.remove(craftSentry.getDescriptor().getSentryId());
    }

    public @Nullable CraftSentry getBySentryId(UUID sentryUUID) {
        return registeredSentries.get(sentryUUID);
    }

    /**
     * Gets the hash map which contains all the
     * registered sentries which are valid on the server.
     *
     * @return the registered sentries hash map
     */
    public HashMap<UUID, CraftSentry> getRegisteredSentries() {
        return registeredSentries;
    }

    /**
     * Gets all registered sentry by the owner's UUID.
     *
     * @param ownerId the UUID of the user we search for
     * @return a map containing all the sentries that the user has owner access,
     * or an empty hash map if none exists.
     */
    public HashMap<UUID, CraftSentry> getPlayerSentries(UUID ownerId) {
        HashMap<UUID, CraftSentry> owned = new HashMap<>();

        registeredSentries.forEach((id, sentry) -> {
            if (!sentry.getDescriptor().getSentryOwners().contains(ownerId)) {
                return;
            }

            owned.put(id, sentry);
        });

        return owned;
    }
}
