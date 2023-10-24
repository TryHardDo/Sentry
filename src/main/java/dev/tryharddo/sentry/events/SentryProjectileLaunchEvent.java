package dev.tryharddo.sentry.events;

import dev.tryharddo.sentry.creatures.CraftSentry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

public class SentryProjectileLaunchEvent extends ProjectileLaunchEvent {
    private final CraftSentry launcherSentry;

    public SentryProjectileLaunchEvent(@NotNull Entity what, CraftSentry source) {
        super(what);
        this.launcherSentry = source;
    }

    public CraftSentry getLauncherSentry() {
        return launcherSentry;
    }
}
