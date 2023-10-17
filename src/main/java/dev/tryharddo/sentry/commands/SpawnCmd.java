package dev.tryharddo.sentry.commands;

import dev.tryharddo.sentry.Sentry;
import dev.tryharddo.sentry.creatures.EntitySentry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCmd implements CommandExecutor {
    private final Sentry sentry;

    public SpawnCmd(Sentry sentry) {
        this.sentry = sentry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("§cEzt a parancsot csak játékosként használhatod!");
            return true;
        }

        Player sender = ((Player) commandSender);
        Location senderLoc = sender.getLocation();
        World serverLevel = senderLoc.getWorld();

        if (serverLevel == null) {
            sender.sendMessage("§cPlayer's world was null!");
            return true;
        }

        EntitySentry entSentry = new EntitySentry(senderLoc, sender.getUniqueId());
        sentry.getSentryRegistry().registerSentry(entSentry);

        sender.sendMessage("§aSpawn command execution must be a success!");

        return true;
    }
}
