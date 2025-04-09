package dev.tn3w.paperafk;

import dev.tn3w.paperafk.commands.AfkCommand;
import dev.tn3w.paperafk.listeners.AfkEventListener;
import dev.tn3w.paperafk.services.AfkService;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperAfk extends JavaPlugin {
    private AfkService afkService;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        
        afkService = new AfkService(this, configManager);
        
        getLogger().info("Setting up AFK world and cleaning up old rooms...");
        afkService.setupAfkWorld();
        
        getCommand("afk").setExecutor(new AfkCommand(afkService, configManager));
        getServer().getPluginManager().registerEvents(new AfkEventListener(afkService, configManager), this);
        getLogger().info("PaperAfk has been enabled!");
    }

    @Override
    public void onDisable() {
        if (afkService != null) {
            afkService.cleanupBeforeShutdown();
        }
        
        getLogger().info("PaperAfk has been disabled!");
    }
    
    /**
     * Get the configuration manager
     * @return The ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
} 