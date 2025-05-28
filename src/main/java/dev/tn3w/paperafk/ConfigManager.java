package dev.tn3w.paperafk;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    /**
     * Gets the name of the world where AFK rooms are created
     *
     * @return The name of the AFK world
     */
    public String getAfkWorldName() {
        return config.getString("afk-world-name", "afk_world");
    }

    /**
     * Gets the size of individual AFK rooms in blocks
     *
     * @return The room size (both length and width)
     */
    public int getRoomSize() {
        return config.getInt("room-size", 10);
    }

    /**
     * Gets the distance between different players' AFK rooms
     *
     * @return The minimum distance between rooms
     */
    public int getRoomDistance() {
        return config.getInt("room-distance", 100);
    }

    /**
     * Checks if players need the 'paperafk.afk' permission to use /afk
     *
     * @return True if permission is required, false if anyone can use it
     */
    public boolean isAfkCommandPermissionRequired() {
        return config.getBoolean("require-afk-permission", false);
    }

    /**
     * Checks if staff need 'paperafk.afk.other' permission to toggle AFK status for other players
     *
     * @return True if permission is required to use /afk on others
     */
    public boolean isAfkOtherCommandPermissionRequired() {
        return config.getBoolean("require-afk-other-permission", true);
    }

    /**
     * Gets the text indicator to show next to AFK players in the tab list
     *
     * @return The AFK indicator text
     */
    public String getAfkIndicator() {
        return config.getString("afk-indicator", "zZ");
    }

    /**
     * Checks if auto-AFK detection is enabled
     *
     * @return True if auto-AFK is enabled
     */
    public boolean isAutoAfkEnabled() {
        return config.getBoolean("auto-afk.enabled", true);
    }

    /**
     * Gets the time in minutes before a player is automatically marked as AFK
     *
     * @return Time in minutes
     */
    public int getAutoAfkTime() {
        return config.getInt("auto-afk.time-minutes", 5);
    }

    /**
     * Checks if the AFK overlay should be shown for auto-AFK
     *
     * @return True if overlay should be shown
     */
    public boolean showAfkOverlay() {
        return config.getBoolean("auto-afk.show-overlay", true);
    }

    /**
     * Gets the time in seconds a player must wait after combat before using /afk
     *
     * @return Combat cooldown in seconds
     */
    public int getCombatCooldownSeconds() {
        return config.getInt("combat.cooldown-seconds", 30);
    }

    /**
     * Checks if administrators can bypass the combat cooldown
     *
     * @return True if admins can bypass combat cooldown
     */
    public boolean canAdminsBypassCombatCooldown() {
        return config.getBoolean("combat.admin-bypass", true);
    }
}
