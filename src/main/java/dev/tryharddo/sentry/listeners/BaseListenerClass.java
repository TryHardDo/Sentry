package dev.tryharddo.sentry.listeners;

import dev.tryharddo.sentry.Sentry;
import org.bukkit.event.Listener;

public class BaseListenerClass implements Listener {
    private final Sentry sentry;

    public BaseListenerClass(Sentry sentry) {
        this.sentry = sentry;
    }
}
