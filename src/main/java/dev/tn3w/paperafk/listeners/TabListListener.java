package dev.tn3w.paperafk.listeners;

import dev.tn3w.paperafk.services.AfkService;
import dev.tn3w.paperafk.services.TabListManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TabListListener implements Listener {
  private final TabListManager tabListManager;
  private final AfkService afkService;

  public TabListListener(TabListManager tabListManager, AfkService afkService) {
    this.tabListManager = tabListManager;
    this.afkService = afkService;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    tabListManager.updatePlayerTabList(event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    // Nothing needed here as players are automatically removed from teams when they disconnect
  }
}
