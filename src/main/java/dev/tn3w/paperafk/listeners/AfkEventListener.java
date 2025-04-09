package dev.tn3w.paperafk.listeners;

import dev.tn3w.paperafk.ConfigManager;
import dev.tn3w.paperafk.services.AfkService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import dev.tn3w.paperafk.gui.JukeboxGUI;

public class AfkEventListener implements Listener {
    private final AfkService afkService;
    private final ConfigManager configManager;

    public AfkEventListener(AfkService afkService, ConfigManager configManager) {
        this.afkService = afkService;
        this.configManager = configManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (world.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot break blocks in the AFK world.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (world.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot place blocks in the AFK world.");
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (world.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot drop items in the AFK world.");
        }
    }
    
    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        World world = player.getWorld();
        
        if (world.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
            if (event.getItem().getLocation().distance(player.getLocation()) < 2) {
                player.sendMessage("§cYou cannot pick up items in the AFK world.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        World world = player.getWorld();
        
        if (world.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        Player damager = null;
        
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }
        
        if (damager == null) {
            return;
        }
        
        World damagerWorld = damager.getWorld();
        World victimWorld = event.getEntity().getWorld();
        
        if (damagerWorld.getName().equals(configManager.getAfkWorldName()) || 
            victimWorld.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
            damager.sendMessage("§cPvP is disabled in the AFK world.");
        }
    }
    
    @EventHandler
    public void onEnderPearlThrow(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        
        if (projectile instanceof EnderPearl) {
            if (projectile.getShooter() instanceof Player) {
                Player player = (Player) projectile.getShooter();
                World world = player.getWorld();
                
                if (world.getName().equals(configManager.getAfkWorldName())) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou cannot use ender pearls in the AFK world.");
                }
            }
        }
    }
    
    @EventHandler
    public void onChorusFruitEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (event.getItem().getType() == Material.CHORUS_FRUIT &&
            world.getName().equals(configManager.getAfkWorldName())) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot eat chorus fruit in the AFK world.");
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (afkService.isPlayerAfk(player)) {
            afkService.setPlayerAfk(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        if (!world.getName().equals(configManager.getAfkWorldName())) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && clickedBlock.getType() == Material.JUKEBOX) {            
            event.setCancelled(true);
            
            JukeboxGUI.openJukeboxMenu(player, clickedBlock);
        }
    }
}