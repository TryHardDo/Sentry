package dev.tryharddo.sentry;

import dev.tryharddo.sentry.commands.SpawnCmd;
import dev.tryharddo.sentry.listeners.BaseListenerClass;
import dev.tryharddo.sentry.listeners.ListenerClass;
import dev.tryharddo.sentry.registries.EntitySentryRegistry;
import dev.tryharddo.sentry.runnables.SentryTickerTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public final class Sentry extends JavaPlugin {
    private static final boolean DEBUG_MODE = true;
    private static Sentry instance;
    private final EntitySentryRegistry sentryRegistry;

    public Sentry() {
        this.sentryRegistry = new EntitySentryRegistry();
    }

    public static Sentry getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.registerEventListeners();
        this.registerCommandExecutors();
        this.startPluginBoundServiceSchedulers();
        this.displayPluginInformation(Bukkit.getConsoleSender());
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("Sentry plugin disabled!");
    }

    private void registerEventListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BaseListenerClass(this), this);
        pluginManager.registerEvents(new ListenerClass(this), this);
    }

    private void startPluginBoundServiceSchedulers() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new SentryTickerTask(sentryRegistry), 0, 1);
    }

    private void registerCommandExecutors() {
        this.getCommand("sentry").setExecutor(new SpawnCmd(this));
    }

    private void displayPluginInformation(@NotNull CommandSender messageTarget) {
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        String[] stringCompound = new String[]{
                "§c§lSentry §f- V§6" + pluginDescriptionFile.getVersion(),
                "§fAuthors: §a" + String.join("§f, §a", pluginDescriptionFile.getAuthors()),
                "§fCompatible API version: " + pluginDescriptionFile.getAPIVersion()
        };

        messageTarget.sendMessage(stringCompound);
    }

    public EntitySentryRegistry getSentryRegistry() {
        return sentryRegistry;
    }
}
