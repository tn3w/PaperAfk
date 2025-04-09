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
     * @return The name of the AFK world
     */
    public String getAfkWorldName() {
        return config.getString("afk-world-name", "afk_world");
    }

    /**
     * Gets the size of individual AFK rooms in blocks
     * @return The room size (both length and width)
     */
    public int getRoomSize() {
        return config.getInt("room-size", 10);
    }

    /**
     * Gets the distance between different players' AFK rooms
     * @return The minimum distance between rooms
     */
    public int getRoomDistance() {
        return config.getInt("room-distance", 100);
    }

    /**
     * Checks if players need the 'paperafk.afk' permission to use /afk
     * @return True if permission is required, false if anyone can use it
     */
    public boolean isAfkCommandPermissionRequired() {
        return config.getBoolean("require-afk-permission", false);
    }

    /**
     * Checks if staff need 'paperafk.afk.other' permission to toggle AFK on other players
     * @return True if permission is required to use /afk on others
     */
    public boolean isAfkOtherCommandPermissionRequired() {
        return config.getBoolean("require-afk-other-permission", true);
    }
} 