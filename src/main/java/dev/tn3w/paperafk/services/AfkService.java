package dev.tn3w.paperafk.services;

import dev.tn3w.paperafk.ConfigManager;
import dev.tn3w.paperafk.PaperAfk;
import dev.tn3w.paperafk.gui.JukeboxGUI;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;

public class AfkService {
  private final PaperAfk plugin;
  private final ConfigManager configManager;
  private final Map<UUID, Location> previousLocations = new HashMap<>();
  private final Map<UUID, Location> afkRoomLocations = new HashMap<>();
  private final Map<UUID, Boolean> playerAfkStatus = new HashMap<>();
  private World afkWorld;

  public AfkService(PaperAfk plugin, ConfigManager configManager) {
    this.plugin = plugin;
    this.configManager = configManager;

    JukeboxGUI.initialize(plugin);
  }

  public void setupAfkWorld() {
    String worldName = configManager.getAfkWorldName();

    afkWorld = Bukkit.getWorld(worldName);

    if (afkWorld != null) {
      plugin.getLogger().info("Unloading existing AFK world to recreate it...");

      for (Player player : afkWorld.getPlayers()) {
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        player.sendMessage("§cThe AFK world is being reset. You have been teleported to spawn.");
        playerAfkStatus.put(player.getUniqueId(), false);
      }

      if (Bukkit.unloadWorld(afkWorld, false)) {
        plugin.getLogger().info("Successfully unloaded AFK world.");

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (deleteWorldFolder(worldFolder)) {
          plugin.getLogger().info("Successfully deleted AFK world folder.");
        } else {
          plugin.getLogger().warning("Failed to delete AFK world folder. Some files may remain.");
        }
      } else {
        plugin.getLogger().warning("Failed to unload AFK world. Trying to continue anyway...");
      }
    }

    File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
    if (worldFolder.exists()) {
      plugin.getLogger().info("Forcibly removing any remaining AFK world files...");
      if (!deleteWorldFolder(worldFolder)) {
        plugin
            .getLogger()
            .warning("Could not completely delete the AFK world folder. Some files may remain.");
      }
    }

    afkRoomLocations.clear();

    plugin.getLogger().info("Creating new AFK world: " + worldName);
    WorldCreator creator = new WorldCreator(worldName);
    creator.generator(new VoidWorldGenerator());
    afkWorld = creator.createWorld();

    if (afkWorld != null) {
      afkWorld.setSpawnLocation(0, 100, 0);
      afkWorld.setAutoSave(true);
      afkWorld.setPVP(false);
      afkWorld.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
      afkWorld.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
      afkWorld.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
      afkWorld.setTime(6000);
    }
  }

  private boolean deleteWorldFolder(File folder) {
    boolean success = true;
    if (folder.exists()) {
      File[] files = folder.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            if (!deleteWorldFolder(file)) {
              success = false;
            }
          } else {
            if (!file.delete()) {
              plugin.getLogger().warning("Failed to delete file: " + file.getAbsolutePath());
              success = false;
            }
          }
        }
      }

      if (!folder.delete()) {
        plugin.getLogger().warning("Failed to delete folder: " + folder.getAbsolutePath());
        success = false;
      }
    }
    return success;
  }

  public boolean isPlayerAfk(Player player) {
    UUID playerId = player.getUniqueId();
    if (playerAfkStatus.containsKey(playerId)) {
      return playerAfkStatus.get(playerId);
    }
    World playerWorld = player.getWorld();
    if (playerWorld != null && playerWorld.getName().equals(configManager.getAfkWorldName())) {
      playerAfkStatus.put(playerId, true);
      return true;
    }
    return false;
  }

  public void setPlayerAfk(Player player) {
    UUID playerId = player.getUniqueId();

    if (isPlayerAfk(player)) {
      if (previousLocations.containsKey(playerId)) {
        Location previousLocation = previousLocations.get(playerId);
        player.teleport(previousLocation);
        previousLocations.remove(playerId);
      } else {
        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        plugin
            .getLogger()
            .warning(
                "Previous location for player "
                    + player.getName()
                    + " not found, teleporting to spawn.");
      }

      playerAfkStatus.put(playerId, false);
      player.sendMessage("§aYou are no longer AFK.");
    } else {
      previousLocations.put(playerId, player.getLocation());
      playerAfkStatus.put(playerId, true);

      Location afkLocation = getOrCreateAfkRoom(player);
      player.teleport(afkLocation);
      player.sendMessage("§aYou are now AFK.");

      Bukkit.getScheduler()
          .runTaskLater(
              plugin,
              () -> {
                for (int x = -10; x <= 10; x++) {
                  for (int y = -5; y <= 5; y++) {
                    for (int z = -10; z <= 10; z++) {
                      Block block = afkLocation.getBlock().getRelative(x, y, z);
                      if (block.getType() == Material.JUKEBOX) {
                        Jukebox jukebox = (Jukebox) block.getState();
                        JukeboxGUI.applyPlayerPreference(player, jukebox);
                        return;
                      }
                    }
                  }
                }
              },
              20L);
    }
  }

  private Location getOrCreateAfkRoom(Player player) {
    UUID playerId = player.getUniqueId();

    if (afkRoomLocations.containsKey(playerId)) {
      return afkRoomLocations.get(playerId);
    }

    plugin.getLogger().info("Creating new AFK room for player: " + player.getName());

    int roomDistance = configManager.getRoomDistance();
    int roomIndex = afkRoomLocations.size();

    int sqrt = (int) Math.sqrt(roomIndex);
    int ringNumber = (sqrt + 1) / 2;
    int positionInRing = roomIndex - (sqrt * sqrt);

    int x, z;

    if (sqrt % 2 == 0) {
      if (positionInRing < sqrt + 1) {
        x = ringNumber;
        z = -ringNumber + positionInRing;
      } else {
        x = ringNumber - (positionInRing - sqrt);
        z = ringNumber;
      }
    } else {
      if (positionInRing < sqrt + 1) {
        x = -ringNumber + positionInRing;
        z = -ringNumber;
      } else {
        x = -ringNumber;
        z = -ringNumber + (positionInRing - sqrt);
      }
    }

    x *= roomDistance;
    z *= roomDistance;
    int y = 100;

    Location roomLocation = new Location(afkWorld, x, y, z);

    createAfkRoom(roomLocation, player.getName());

    Location centerLocation =
        roomLocation
            .clone()
            .add(configManager.getRoomSize() / 2, 1, configManager.getRoomSize() / 2);
    afkRoomLocations.put(playerId, centerLocation);

    return centerLocation;
  }

  /**
   * Performs cleanup operations before the plugin shuts down This helps ensure proper state for the
   * next server start
   */
  public void cleanupBeforeShutdown() {
    plugin.getLogger().info("Performing cleanup before shutdown...");

    for (UUID playerId : playerAfkStatus.keySet()) {
      if (playerAfkStatus.get(playerId)) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
          if (previousLocations.containsKey(playerId)) {
            player.teleport(previousLocations.get(playerId));
            player.sendMessage("§cYou have been returned from AFK due to server shutdown.");
          } else {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
          }
        }
      }
    }

    previousLocations.clear();
    afkRoomLocations.clear();
    playerAfkStatus.clear();
  }

  private void createAfkRoom(Location location, String playerName) {
    int roomSize = configManager.getRoomSize();
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();

    for (int dx = 0; dx < roomSize; dx++) {
      for (int dz = 0; dz < roomSize; dz++) {
        for (int dy = 0; dy < 5; dy++) {
          Material material;

          if (dy == 0) {
            if ((dx + dz) % 2 == 0) {
              material = Material.POLISHED_ANDESITE;
            } else {
              material = Material.POLISHED_DIORITE;
            }
          } else if (dy == 4) {
            boolean isCorner =
                (dx < 4 && dz < 4)
                    || (dx < 4 && dz >= roomSize - 4)
                    || (dx >= roomSize - 4 && dz < 4)
                    || (dx >= roomSize - 4 && dz >= roomSize - 4);
            boolean isEdge = dx == 0 || dx == roomSize - 1 || dz == 0 || dz == roomSize - 1;

            if (isEdge || dx == roomSize / 2 || dz == roomSize / 2) {
              material = Material.SMOOTH_QUARTZ;
            } else if (isCorner) {
              material = Material.SMOOTH_STONE;
            } else {
              material = Material.SMOOTH_QUARTZ;
            }
          } else if (dx == 0 || dx == roomSize - 1 || dz == 0 || dz == roomSize - 1) {
            if (dy == 2
                && ((dx == roomSize / 4 && (dz == 0 || dz == roomSize - 1))
                    || (dx == roomSize * 3 / 4 && (dz == 0 || dz == roomSize - 1))
                    || (dz == roomSize / 4 && (dx == 0 || dx == roomSize - 1))
                    || (dz == roomSize * 3 / 4 && (dx == 0 || dx == roomSize - 1)))) {
              material = Material.SEA_LANTERN;
            } else {
              if (dy == 1) {
                material = Material.CYAN_TERRACOTTA;
              } else if (dy == 2) {
                material = Material.LIGHT_BLUE_TERRACOTTA;
              } else {
                material = Material.BLUE_TERRACOTTA;
              }
            }
          } else {
            material = Material.AIR;
            continue;
          }

          afkWorld.getBlockAt(x + dx, y + dy, z + dz).setType(material);
        }
      }
    }

    int centerX = x + roomSize / 2;
    int centerZ = z + roomSize / 2;
    afkWorld.getBlockAt(centerX, y + 4, centerZ).setType(Material.GLASS);
    afkWorld.getBlockAt(centerX + 1, y + 4, centerZ).setType(Material.GLASS);
    afkWorld.getBlockAt(centerX - 1, y + 4, centerZ).setType(Material.GLASS);
    afkWorld.getBlockAt(centerX, y + 4, centerZ + 1).setType(Material.GLASS);
    afkWorld.getBlockAt(centerX, y + 4, centerZ - 1).setType(Material.GLASS);

    afkWorld.getBlockAt(centerX, y + 3, centerZ).setType(Material.LANTERN);
    org.bukkit.block.data.type.Lantern lanternData =
        (org.bukkit.block.data.type.Lantern)
            afkWorld.getBlockAt(centerX, y + 3, centerZ).getBlockData();
    lanternData.setHanging(true);
    afkWorld.getBlockAt(centerX, y + 3, centerZ).setBlockData(lanternData);

    Location couchLoc1 = new Location(afkWorld, x + roomSize - 2, y + 1, z + 2);
    afkWorld.getBlockAt(couchLoc1).setType(Material.SPRUCE_STAIRS);
    org.bukkit.block.data.Directional couchData1 =
        (org.bukkit.block.data.Directional) afkWorld.getBlockAt(couchLoc1).getBlockData();
    couchData1.setFacing(BlockFace.EAST);
    afkWorld.getBlockAt(couchLoc1).setBlockData(couchData1);

    Location couchLoc2 = new Location(afkWorld, x + roomSize - 2, y + 1, z + 3);
    afkWorld.getBlockAt(couchLoc2).setType(Material.SPRUCE_STAIRS);
    org.bukkit.block.data.Directional couchData2 =
        (org.bukkit.block.data.Directional) afkWorld.getBlockAt(couchLoc2).getBlockData();
    couchData2.setFacing(BlockFace.EAST);
    afkWorld.getBlockAt(couchLoc2).setBlockData(couchData2);

    Location nightstandLoc = new Location(afkWorld, x + roomSize - 3, y + 1, z + 2);
    afkWorld.getBlockAt(nightstandLoc).setType(Material.SPRUCE_PLANKS);
    afkWorld.getBlockAt(nightstandLoc.clone().add(0, 1, 0)).setType(Material.LANTERN);
    org.bukkit.block.data.type.Lantern tableLanternData =
        (org.bukkit.block.data.type.Lantern)
            afkWorld.getBlockAt(nightstandLoc.clone().add(0, 1, 0)).getBlockData();
    tableLanternData.setHanging(false);
    afkWorld.getBlockAt(nightstandLoc.clone().add(0, 1, 0)).setBlockData(tableLanternData);

    Location shelfLoc1 = new Location(afkWorld, x + roomSize - 2, y + 1, z + roomSize - 2);
    Location shelfLoc2 = new Location(afkWorld, x + roomSize - 3, y + 1, z + roomSize - 2);
    afkWorld.getBlockAt(shelfLoc1).setType(Material.BOOKSHELF);
    afkWorld.getBlockAt(shelfLoc2).setType(Material.BOOKSHELF);
    afkWorld.getBlockAt(shelfLoc1.clone().add(0, 1, 0)).setType(Material.BOOKSHELF);
    afkWorld.getBlockAt(shelfLoc2.clone().add(0, 1, 0)).setType(Material.BOOKSHELF);

    Location deskLoc = new Location(afkWorld, x + roomSize - 4, y + 1, z + roomSize - 2);
    afkWorld.getBlockAt(deskLoc).setType(Material.DARK_OAK_PLANKS);
    afkWorld.getBlockAt(deskLoc.clone().add(0, 1, 0)).setType(Material.SOUL_LANTERN);
    org.bukkit.block.data.type.Lantern soulLanternData =
        (org.bukkit.block.data.type.Lantern)
            afkWorld.getBlockAt(deskLoc.clone().add(0, 1, 0)).getBlockData();
    soulLanternData.setHanging(false);
    afkWorld.getBlockAt(deskLoc.clone().add(0, 1, 0)).setBlockData(soulLanternData);

    Location chairLoc = new Location(afkWorld, x + roomSize - 4, y + 1, z + roomSize - 3);
    afkWorld.getBlockAt(chairLoc).setType(Material.SPRUCE_STAIRS);
    org.bukkit.block.data.Directional chairData =
        (org.bukkit.block.data.Directional) afkWorld.getBlockAt(chairLoc).getBlockData();
    chairData.setFacing(org.bukkit.block.BlockFace.NORTH);
    afkWorld.getBlockAt(chairLoc).setBlockData(chairData);

    Location plantLoc1 = new Location(afkWorld, x + 3, y + 1, z + roomSize - 2);
    afkWorld.getBlockAt(plantLoc1).setType(Material.MOSS_BLOCK);
    afkWorld.getBlockAt(plantLoc1.clone().add(0, 1, 0)).setType(Material.FERN);

    for (int dx = roomSize / 4; dx < roomSize * 3 / 4; dx++) {
      for (int dz = roomSize / 4; dz < roomSize * 3 / 4; dz++) {
        afkWorld.getBlockAt(x + dx, y + 1, z + dz).setType(Material.LIGHT_BLUE_CARPET);
      }
    }

    Location tableLoc = new Location(afkWorld, x + roomSize / 2, y + 1, z + roomSize / 2);
    afkWorld.getBlockAt(tableLoc).setType(Material.SPRUCE_FENCE);
    afkWorld.getBlockAt(tableLoc.clone().add(0, 1, 0)).setType(Material.DARK_OAK_PRESSURE_PLATE);

    Location jukeboxLoc = new Location(afkWorld, x + 2, y + 1, z + 2);
    afkWorld.getBlockAt(jukeboxLoc).setType(Material.JUKEBOX);

    org.bukkit.block.Jukebox jukebox =
        (org.bukkit.block.Jukebox) afkWorld.getBlockAt(jukeboxLoc).getState();

    Player player = Bukkit.getPlayerExact(playerName);
    if (player != null) {
      JukeboxGUI.applyPlayerPreference(player, jukebox);
    }

    Location enderChestLoc = new Location(afkWorld, x + 2, y + 1, z + 3);
    afkWorld.getBlockAt(enderChestLoc).setType(Material.ENDER_CHEST);
  }
}
