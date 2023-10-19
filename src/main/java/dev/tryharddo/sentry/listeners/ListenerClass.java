package dev.tryharddo.sentry.listeners;

import dev.tryharddo.sentry.Sentry;
import dev.tryharddo.sentry.events.SentryTargetingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ListenerClass implements Listener {
    private final Sentry sentry;

    public ListenerClass(Sentry sentry) {
        this.sentry = sentry;
    }

    @EventHandler
    public void onTargeting(@NotNull final SentryTargetingEvent e) {
        System.out.println(e.getSentry().getDescriptor().getSentryId() + " is now targeting a " + e.getTarget().getType() + " at " + e.getTarget().getLocation());
    }
}
