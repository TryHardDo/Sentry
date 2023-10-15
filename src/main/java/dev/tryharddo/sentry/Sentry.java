package dev.tryharddo.sentry;

import org.bukkit.plugin.java.JavaPlugin;
import sun.security.jca.GetInstance;

import java.util.logging.Logger;

public final class Sentry extends JavaPlugin {
    private static Sentry instance;
    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Sentry getInstance() {
        return instance;
    }
}
