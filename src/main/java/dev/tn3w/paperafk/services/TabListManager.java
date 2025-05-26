package dev.tn3w.paperafk.services;

import dev.tn3w.paperafk.PaperAfk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabListManager {
    private final PaperAfk plugin;
    private final AfkService afkService;
    private Scoreboard scoreboard;
    private Team afkTeam;

    public TabListManager(PaperAfk plugin, AfkService afkService) {
        this.plugin = plugin;
        this.afkService = afkService;
        setupScoreboard();
    }

    private void setupScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getTeam("afkPlayers") != null) {
            scoreboard.getTeam("afkPlayers").unregister();
        }

        afkTeam = scoreboard.registerNewTeam("afkPlayers");
        String afkIndicator = plugin.getConfigManager().getAfkIndicator();
        afkTeam.suffix(
                Component.text(" ")
                        .append(Component.text(afkIndicator, TextColor.color(0x888888))));
        afkTeam.color(NamedTextColor.GRAY);
    }

    /**
     * Update a player's tab list entry based on their AFK status
     *
     * @param player The player to update
     */
    public void updatePlayerTabList(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (afkService.isPlayerAfk(player)) {
            afkTeam.addEntry(player.getName());
        } else {
            afkTeam.removeEntry(player.getName());
        }
    }

    /** Update the tab list for all online players */
    public void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTabList(player);
        }
    }
}
