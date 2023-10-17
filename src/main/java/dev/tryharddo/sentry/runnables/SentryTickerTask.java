package dev.tryharddo.sentry.runnables;

import dev.tryharddo.sentry.creatures.EntitySentry;
import dev.tryharddo.sentry.registries.EntitySentryRegistry;

import java.util.Iterator;

public class SentryTickerTask implements Runnable {
    private final EntitySentryRegistry registry;

    public SentryTickerTask(EntitySentryRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void run() {
        Iterator<EntitySentry> iterator = registry.getRegisteredSentries().iterator();

        while (iterator.hasNext()) {
            EntitySentry sentry = iterator.next();

            if (!sentry.isValid()) {
                iterator.remove();
                continue;
            }

            if (sentry.isDisabled()) {
                continue;
            }

            sentry.tick();
        }
    }
}
