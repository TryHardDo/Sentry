package dev.tryharddo.sentry;

import dev.tryharddo.sentry.commands.SpawnCmd;
import dev.tryharddo.sentry.registries.EntitySentryRegistry;
import dev.tryharddo.sentry.runnables.SentryTickerTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Sentry extends JavaPlugin {
    private static Sentry instance;
    private final EntitySentryRegistry sentryRegistry = new EntitySentryRegistry();

    public static Sentry getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("sentry").setExecutor(new SpawnCmd(this));
        getLogger().info("Starting global sentry ticker task scheduler...");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SentryTickerTask(sentryRegistry), 0, 1);
        getLogger().info("Sentry plugin ready!");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("Sentry plugin disabled!");
    }

    public EntitySentryRegistry getSentryRegistry() {
        return sentryRegistry;
    }
}
