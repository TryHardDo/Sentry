package dev.tryharddo.sentry.runnables;

import dev.tryharddo.sentry.creatures.EntitySentry;
import dev.tryharddo.sentry.registries.EntitySentryRegistry;

import java.util.Iterator;
import java.util.logging.Logger;

public class SentryTickerTask implements Runnable {
    private final EntitySentryRegistry registry;
    private final Logger logger = Logger.getLogger("SentryTickerTask");

    public SentryTickerTask(EntitySentryRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void run() {
        try {
            Iterator<EntitySentry> iterator = registry.getRegisteredSentries().values().iterator();
            while (iterator.hasNext()) {
                EntitySentry s = iterator.next();
                if (!s.isValid()) {
                    iterator.remove();
                    continue;
                }

                if (s.isDisabled()) {
                    continue;
                }

                s.tick();
            }
        } catch (Exception ex) {
            logger.severe("An error occurred while ticking task got executed! => " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
