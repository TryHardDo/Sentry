package dev.tryharddo.sentry.registries;

import dev.tryharddo.sentry.creatures.EntitySentry;

import java.util.HashSet;

public class EntitySentryRegistry {
    private final HashSet<EntitySentry> registeredSentries;

    public EntitySentryRegistry(HashSet<EntitySentry> sentries) {
        registeredSentries = sentries;
    }

    public EntitySentryRegistry() {
        registeredSentries = new HashSet<>();
    }

    public boolean registerSentry(EntitySentry entitySentry) {
        return registeredSentries.add(entitySentry);
    }

    public boolean unregisterSentry(EntitySentry entitySentry) {
        return registeredSentries.remove(entitySentry);
    }

    public HashSet<EntitySentry> getRegisteredSentries() {
        return registeredSentries;
    }

    public int validateRegistry() {
        int removed = 0;
        for (EntitySentry es : registeredSentries) {
            if (es.isValid()) {
                continue;
            }

            registeredSentries.remove(es);
            removed++;
        }

        return removed;
    }
}
