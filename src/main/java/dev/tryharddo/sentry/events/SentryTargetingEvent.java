package dev.tryharddo.sentry.events;

import dev.tryharddo.sentry.creatures.CraftSentry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SentryTargetingEvent extends EntityTargetEvent {
    private final CraftSentry craftSentry;

    public SentryTargetingEvent(@NotNull CraftSentry entity, @Nullable Entity target, @NotNull EntityTargetEvent.TargetReason reason) {
        super(entity.getSentryBody(), target, reason);
        this.craftSentry = entity;
    }

    public CraftSentry getSentry() {
        return craftSentry;
    }
}
