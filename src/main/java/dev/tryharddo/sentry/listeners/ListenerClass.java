package dev.tryharddo.sentry.listeners;

import dev.tryharddo.sentry.Sentry;
import org.bukkit.event.Listener;

public class ListenerClass implements Listener {
    private final Sentry sentry;

    public ListenerClass(Sentry sentry) {
        this.sentry = sentry;
    }
}
