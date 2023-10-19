package dev.tryharddo.sentry.events;

import dev.tryharddo.sentry.creatures.EntitySentry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

public class SentryProjectileLaunchEvent extends ProjectileLaunchEvent {
    private final EntitySentry launcherSentry;

    public SentryProjectileLaunchEvent(@NotNull Entity what, EntitySentry source) {
        super(what);
        this.launcherSentry = source;
    }

    public EntitySentry getLauncherSentry() {
        return launcherSentry;
    }
}
