package dev.tn3w.paperafk.services;

import dev.tn3w.paperafk.ConfigManager;
import dev.tn3w.paperafk.PaperAfk;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final PaperAfk plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Long> combatTimestamps = new HashMap<>();

    public CombatManager(PaperAfk plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Tag a player as being in combat
     *
     * @param player The player to tag
     */
    public void tagPlayerCombat(Player player) {
        combatTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Check if a player is in combat cooldown
     *
     * @param player The player to check
     * @return True if the player is in combat cooldown and cannot use /afk
     */
    public boolean isInCombat(Player player) {
        if (player.hasPermission("paperafk.bypass.combat")
                && configManager.canAdminsBypassCombatCooldown()) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (!combatTimestamps.containsKey(playerId)) {
            return false;
        }

        long lastCombatTime = combatTimestamps.get(playerId);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = configManager.getCombatCooldownSeconds() * 1000L;

        return (currentTime - lastCombatTime) < cooldownMs;
    }

    /**
     * Get the remaining cooldown time in seconds for a player
     *
     * @param player The player to check
     * @return Remaining cooldown time in seconds, or 0 if not in combat
     */
    public int getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!combatTimestamps.containsKey(playerId)) {
            return 0;
        }

        long lastCombatTime = combatTimestamps.get(playerId);
        long currentTime = System.currentTimeMillis();
        long cooldownMs = configManager.getCombatCooldownSeconds() * 1000L;
        long remainingMs = cooldownMs - (currentTime - lastCombatTime);

        if (remainingMs <= 0) {
            combatTimestamps.remove(playerId);
            return 0;
        }

        return (int) (remainingMs / 1000);
    }

    /**
     * Clear combat tag for a player
     *
     * @param player The player to clear the combat tag for
     */
    public void clearCombatTag(Player player) {
        combatTimestamps.remove(player.getUniqueId());
    }
}
