package dev.tn3w.paperafk.commands;

import dev.tn3w.paperafk.ConfigManager;
import dev.tn3w.paperafk.services.AfkService;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    private final AfkService afkService;
    private final ConfigManager configManager;

    public AfkCommand(AfkService afkService, ConfigManager configManager) {
        this.afkService = afkService;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cThis command can only be executed by a player.");
                return true;
            }

            Player player = (Player) sender;

            if (configManager.isAfkCommandPermissionRequired()
                    && !player.hasPermission("paperafk.afk")) {
                player.sendMessage("§cYou don't have permission to use this command.");
                return true;
            }

            afkService.setPlayerAfk(player);
            return true;
        } else if (args.length == 1) {
            if (!sender.hasPermission("paperafk.afk.other")) {
                sender.sendMessage("§cYou don't have permission to set other players AFK.");
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage("§cPlayer not found or not online.");
                return true;
            }

            afkService.setPlayerAfk(targetPlayer);
            sender.sendMessage("§aAFK status toggled for player " + targetPlayer.getName() + ".");
            return true;
        }

        return false;
    }
}
