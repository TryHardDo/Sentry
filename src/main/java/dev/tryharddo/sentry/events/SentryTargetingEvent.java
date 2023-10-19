package dev.tryharddo.sentry.events;

import dev.tryharddo.sentry.creatures.EntitySentry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SentryTargetingEvent extends EntityTargetEvent {
    private final EntitySentry entitySentry;

    public SentryTargetingEvent(@NotNull EntitySentry entity, @Nullable Entity target, @NotNull EntityTargetEvent.TargetReason reason) {
        super(entity.getSentryBody(), target, reason);
        this.entitySentry = entity;
    }

    public EntitySentry getSentry() {
        return entitySentry;
    }
}
