package dev.tn3w.paperafk;

import dev.tn3w.paperafk.commands.AfkCommand;
import dev.tn3w.paperafk.listeners.AfkEventListener;
import dev.tn3w.paperafk.listeners.CombatListener;
import dev.tn3w.paperafk.listeners.TabListListener;
import dev.tn3w.paperafk.services.AfkService;
import dev.tn3w.paperafk.services.CombatManager;
import dev.tn3w.paperafk.services.TabListManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperAfk extends JavaPlugin {
    private AfkService afkService;
    private TabListManager tabListManager;
    private ConfigManager configManager;
    private CombatManager combatManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);

        afkService = new AfkService(this, configManager);
        combatManager = new CombatManager(this, configManager);

        getLogger().info("Setting up AFK world and cleaning up old rooms...");
        afkService.setupAfkWorld();

        tabListManager = new TabListManager(this, afkService);
        afkService.setTabListManager(tabListManager);

        tabListManager.updateAllPlayers();

        getCommand("afk").setExecutor(new AfkCommand(afkService, configManager, combatManager));

        getServer()
                .getPluginManager()
                .registerEvents(new AfkEventListener(afkService, configManager), this);
        getServer()
                .getPluginManager()
                .registerEvents(new TabListListener(tabListManager, afkService), this);
        getServer().getPluginManager().registerEvents(new CombatListener(combatManager), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            afkService.updatePlayerActivity(player);
        }

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
     *
     * @return The ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
