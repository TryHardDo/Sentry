package dev.tryharddo.sentry.registries;

import dev.tryharddo.sentry.creatures.EntitySentry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class EntitySentryRegistry {
    private final HashMap<UUID, EntitySentry> registeredSentries;

    public EntitySentryRegistry(HashMap<UUID, EntitySentry> sentries) {
        registeredSentries = sentries;
    }

    public EntitySentryRegistry() {
        registeredSentries = new HashMap<>();
    }

    /**
     * Registers an EntitySentry in the sentry registry.
     *
     * @param entitySentry the sentry entity
     * @return null if no swap happened or the previous value was null
     * or the swapped EntitySentry object.
     */
    public @Nullable EntitySentry registerSentry(@NotNull EntitySentry entitySentry) {
        return registeredSentries.put(entitySentry.getDescriptor().getSentryId(), entitySentry);
    }

    public EntitySentry unregisterSentry(@NotNull EntitySentry entitySentry) {
        return registeredSentries.remove(entitySentry.getDescriptor().getSentryId());
    }

    public @Nullable EntitySentry getBySentryId(UUID sentryUUID) {
        return registeredSentries.get(sentryUUID);
    }

    /**
     * Gets the hash map which contains all the
     * registered sentries which are valid on the server.
     *
     * @return the registered sentries hash map
     */
    public HashMap<UUID, EntitySentry> getRegisteredSentries() {
        return registeredSentries;
    }

    /**
     * Gets all registered sentry by the owner's UUID.
     *
     * @param ownerId the UUID of the user we search for
     * @return a map containing all the sentries that the user has owner access,
     * or an empty hash map if none exists.
     */
    public HashMap<UUID, EntitySentry> getPlayerSentries(UUID ownerId) {
        HashMap<UUID, EntitySentry> owned = new HashMap<>();

        registeredSentries.forEach((id, sentry) -> {
            if (!sentry.getDescriptor().getSentryOwners().contains(ownerId)) {
                return;
            }

            owned.put(id, sentry);
        });

        return owned;
    }
}
