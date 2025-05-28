package dev.tn3w.paperafk.listeners;

import dev.tn3w.paperafk.services.CombatManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {
    private final CombatManager combatManager;

    public CombatListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = getAttackerFromDamageEvent(event);

        if (attacker != null && attacker != victim) {
            combatManager.tagPlayerCombat(victim);
            combatManager.tagPlayerCombat(attacker);
        }
    }

    /**
     * Helper method to get the player attacker from a damage event
     *
     * @param event The damage event
     * @return The player attacker, or null if the attacker was not a player
     */
    private Player getAttackerFromDamageEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Player) {
            return (Player) damager;
        }

        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }

        return null;
    }
}
