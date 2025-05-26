package dev.tn3w.paperafk.gui;

import dev.tn3w.paperafk.PaperAfk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JukeboxGUI implements Listener {
    private static final Component INVENTORY_TITLE =
            Component.text("Jukebox - Select Music")
                    .color(net.kyori.adventure.text.format.NamedTextColor.BLUE);
    private static final int INVENTORY_SIZE = 45;

    private static final Map<UUID, Block> playerJukeboxMap = new HashMap<>();
    private static PaperAfk plugin;
    private static final Map<UUID, Material> playerDiscPreferences = new HashMap<>();
    private static NamespacedKey jukeboxPreferenceKey;
    private static Block lastInteractedJukebox;
    private static final Map<Block, Sound> playingJukeboxes = new HashMap<>();

    private static boolean hasCreatorDisc = false;
    private static boolean hasCreatorMusicBoxDisc = false;
    private static boolean hasPrecipiceDisc = false;

    private static Material DISC_CREATOR = null;
    private static Material DISC_CREATOR_MUSIC_BOX = null;
    private static Material DISC_PRECIPICE = null;

    private static final List<Object[]> MUSIC_DISC_INFO_LIST = new ArrayList<>();

    private static final Object[][] MUSIC_DISC_INFO;

    private static final Material[] MUSIC_DISCS;

    private static final Map<Material, Object[]> DISC_INFO_MAP = new HashMap<>();

    static {
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_13, 1, "C418 - 13"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_CAT, 2, "C418 - cat"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_BLOCKS, 3, "C418 - blocks"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_CHIRP, 4, "C418 - chirp"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_FAR, 5, "C418 - far"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_MALL, 6, "C418 - mall"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_MELLOHI, 7, "C418 - mellohi"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_STAL, 8, "C418 - stal"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_STRAD, 9, "C418 - strad"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_WARD, 10, "C418 - ward"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_11, 11, "C418 - 11"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_WAIT, 12, "C418 - wait"});
        MUSIC_DISC_INFO_LIST.add(
                new Object[] {Material.MUSIC_DISC_PIGSTEP, 13, "Lena Raine - Pigstep"});
        MUSIC_DISC_INFO_LIST.add(
                new Object[] {Material.MUSIC_DISC_OTHERSIDE, 14, "Lena Raine - otherside"});
        MUSIC_DISC_INFO_LIST.add(new Object[] {Material.MUSIC_DISC_5, 15, "Samuel Åberg - 5"});
        MUSIC_DISC_INFO_LIST.add(
                new Object[] {Material.MUSIC_DISC_RELIC, 16, "Aaron Cherof - Relic"});

        try {
            DISC_CREATOR = Material.valueOf("MUSIC_DISC_CREATOR");
            hasCreatorDisc = true;
            MUSIC_DISC_INFO_LIST.add(new Object[] {DISC_CREATOR, 17, "Lena Raine - Creator"});
        } catch (IllegalArgumentException e) {
            // DISC_CREATOR not available in this version
        }

        try {
            DISC_CREATOR_MUSIC_BOX = Material.valueOf("MUSIC_DISC_CREATOR_MUSIC_BOX");
            hasCreatorMusicBoxDisc = true;
            MUSIC_DISC_INFO_LIST.add(
                    new Object[] {DISC_CREATOR_MUSIC_BOX, 18, "Lena Raine - Creator (Music Box)"});
        } catch (IllegalArgumentException e) {
            // DISC_CREATOR_MUSIC_BOX not available in this version
        }

        try {
            DISC_PRECIPICE = Material.valueOf("MUSIC_DISC_PRECIPICE");
            hasPrecipiceDisc = true;
            MUSIC_DISC_INFO_LIST.add(new Object[] {DISC_PRECIPICE, 19, "Aaron Cherof - Precipice"});
        } catch (IllegalArgumentException e) {
            // DISC_PRECIPICE not available in this version
        }

        MUSIC_DISC_INFO = MUSIC_DISC_INFO_LIST.toArray(new Object[MUSIC_DISC_INFO_LIST.size()][]);

        MUSIC_DISCS =
                MUSIC_DISC_INFO_LIST.stream()
                        .map(info -> (Material) info[0])
                        .toArray(Material[]::new);

        for (Object[] discInfo : MUSIC_DISC_INFO) {
            DISC_INFO_MAP.put((Material) discInfo[0], discInfo);
        }
    }

    public static void initialize(PaperAfk plugin) {
        JukeboxGUI.plugin = plugin;
        jukeboxPreferenceKey = new NamespacedKey(plugin, "jukebox_preference");

        plugin.getServer().getPluginManager().registerEvents(new JukeboxGUI(), plugin);

        plugin.getLogger().info("JukeboxGUI initialized. Ready to handle music disc playback.");
    }

    /** Opens the jukebox GUI menu for a player */
    public static void openJukeboxMenu(Player player, Block jukeboxBlock) {
        if (plugin == null) {
            throw new IllegalStateException("JukeboxGUI has not been initialized");
        }

        lastInteractedJukebox = jukeboxBlock;

        Inventory inventory =
                player.getServer().createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);

        ItemStack glassFiller = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta glassFillerMeta = glassFiller.getItemMeta();
        glassFillerMeta.displayName(Component.text(" "));
        glassFiller.setItemMeta(glassFillerMeta);

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, glassFiller);
        }

        int startRow = 1;
        int musicDiscsPerRow = 7;
        int totalDiscs = MUSIC_DISC_INFO.length;
        int completeRows = totalDiscs / musicDiscsPerRow;
        int remainingDiscs = totalDiscs % musicDiscsPerRow;

        int lastRowOffset = (musicDiscsPerRow - remainingDiscs) / 2;

        for (int i = 0; i < totalDiscs; i++) {
            int row = i / musicDiscsPerRow;
            int col = i % musicDiscsPerRow;

            if (row == completeRows && remainingDiscs > 0) {
                col += lastRowOffset;
            }

            int slot = (startRow + row) * 9 + 1 + col;

            if (slot < INVENTORY_SIZE) {
                Object[] discInfo = MUSIC_DISC_INFO[i];
                Material discMaterial = (Material) discInfo[0];
                int discId = (int) discInfo[1];
                String discName = (String) discInfo[2];

                ItemStack disc = new ItemStack(discMaterial);
                ItemMeta discMeta = disc.getItemMeta();

                discMeta.displayName(
                        Component.text(discName)
                                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));

                List<Component> lore = new ArrayList<>();
                lore.add(
                        Component.text("Disc #" + discId)
                                .color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
                lore.add(
                        Component.text("Click to play this music disc")
                                .color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
                discMeta.lore(lore);

                disc.setItemMeta(discMeta);
                inventory.setItem(slot, disc);
            }
        }

        ItemStack stopButton = new ItemStack(Material.BARRIER);
        ItemMeta stopMeta = stopButton.getItemMeta();
        stopMeta.displayName(
                Component.text("Stop Music")
                        .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD));
        List<Component> stopLore = new ArrayList<>();
        stopLore.add(
                Component.text("Click to stop any playing music")
                        .color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
        stopMeta.lore(stopLore);
        stopButton.setItemMeta(stopMeta);
        inventory.setItem(INVENTORY_SIZE - 5, stopButton);

        playerJukeboxMap.put(player.getUniqueId(), jukeboxBlock);

        player.openInventory(inventory);
    }

    /** Format the music disc material name to be more user-friendly */
    private static String formatDiscName(String materialName) {
        String nameWithoutPrefix = materialName.replace("MUSIC_DISC_", "");
        String nameWithSpaces = nameWithoutPrefix.replace("_", " ");

        StringBuilder formatted = new StringBuilder();
        boolean nextUpperCase = true;

        for (char c : nameWithSpaces.toCharArray()) {
            if (nextUpperCase) {
                formatted.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                formatted.append(Character.toLowerCase(c));
            }

            if (c == ' ') {
                nextUpperCase = true;
            }
        }

        return formatted.toString();
    }

    /** Get the custom name for a music disc */
    private static String getCustomDiscName(Material discMaterial) {
        Object[] discInfo = DISC_INFO_MAP.get(discMaterial);
        if (discInfo != null) {
            return (String) discInfo[2];
        }
        return formatDiscName(discMaterial.toString());
    }

    /** Get the AFK world name from config */
    private static String getAfkWorldName() {
        return plugin.getConfigManager().getAfkWorldName();
    }

    /** Gets the corresponding sound for a music disc */
    private static Sound getDiscSound(Material discMaterial) {
        try {
            if (discMaterial == Material.MUSIC_DISC_5) return Sound.MUSIC_DISC_5;
            if (discMaterial == Material.MUSIC_DISC_11) return Sound.MUSIC_DISC_11;
            if (discMaterial == Material.MUSIC_DISC_13) return Sound.MUSIC_DISC_13;
            if (discMaterial == Material.MUSIC_DISC_BLOCKS) return Sound.MUSIC_DISC_BLOCKS;
            if (discMaterial == Material.MUSIC_DISC_CAT) return Sound.MUSIC_DISC_CAT;
            if (discMaterial == Material.MUSIC_DISC_CHIRP) return Sound.MUSIC_DISC_CHIRP;
            if (discMaterial == Material.MUSIC_DISC_FAR) return Sound.MUSIC_DISC_FAR;
            if (discMaterial == Material.MUSIC_DISC_MALL) return Sound.MUSIC_DISC_MALL;
            if (discMaterial == Material.MUSIC_DISC_MELLOHI) return Sound.MUSIC_DISC_MELLOHI;
            if (discMaterial == Material.MUSIC_DISC_OTHERSIDE) return Sound.MUSIC_DISC_OTHERSIDE;
            if (discMaterial == Material.MUSIC_DISC_PIGSTEP) return Sound.MUSIC_DISC_PIGSTEP;
            if (discMaterial == Material.MUSIC_DISC_RELIC) return Sound.MUSIC_DISC_RELIC;
            if (discMaterial == Material.MUSIC_DISC_STAL) return Sound.MUSIC_DISC_STAL;
            if (discMaterial == Material.MUSIC_DISC_STRAD) return Sound.MUSIC_DISC_STRAD;
            if (discMaterial == Material.MUSIC_DISC_WAIT) return Sound.MUSIC_DISC_WAIT;
            if (discMaterial == Material.MUSIC_DISC_WARD) return Sound.MUSIC_DISC_WARD;

            if (hasCreatorDisc && discMaterial == DISC_CREATOR) {
                try {
                    for (Sound sound : getSounds()) {
                        if (sound.toString().contains("MUSIC_DISC_CREATOR")) {
                            return sound;
                        }
                    }
                    return Sound.MUSIC_DISC_WAIT;
                } catch (Exception ex) {
                    return Sound.MUSIC_DISC_WAIT;
                }
            }

            if (hasCreatorMusicBoxDisc && discMaterial == DISC_CREATOR_MUSIC_BOX) {
                try {
                    for (Sound sound : getSounds()) {
                        if (sound.toString().contains("MUSIC_DISC_CREATOR_MUSIC_BOX")) {
                            return sound;
                        }
                    }
                    return Sound.MUSIC_DISC_13;
                } catch (Exception ex) {
                    return Sound.MUSIC_DISC_13;
                }
            }

            if (hasPrecipiceDisc && discMaterial == DISC_PRECIPICE) {
                try {
                    for (Sound sound : getSounds()) {
                        if (sound.toString().contains("MUSIC_DISC_PRECIPICE")) {
                            return sound;
                        }
                    }
                    return Sound.MUSIC_DISC_PIGSTEP;
                } catch (Exception ex) {
                    return Sound.MUSIC_DISC_PIGSTEP;
                }
            }

            String materialName = discMaterial.name();
            for (Sound sound : getSounds()) {
                if (sound.toString().equals(materialName)) {
                    return sound;
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Helper method to safely get all Sound values */
    @SuppressWarnings("deprecation")
    private static Sound[] getSounds() {
        return Sound.values();
    }

    /** Safely stops any playing music at a location and cleans up the record state */
    private static void stopMusic(Block jukeboxBlock) {
        if (jukeboxBlock != null && jukeboxBlock.getType() == Material.JUKEBOX) {
            Jukebox jukebox = (Jukebox) jukeboxBlock.getState();

            playingJukeboxes.remove(jukeboxBlock);

            boolean hasDisc = jukebox.getRecord() != null && !jukebox.getRecord().getType().isAir();

            jukebox.setRecord(null);
            jukebox.update(true, true);

            if (hasDisc) {
                Location jbLoc = jukeboxBlock.getLocation();
                jukeboxBlock
                        .getWorld()
                        .getNearbyEntities(jbLoc, 3, 3, 3)
                        .forEach(
                                entity -> {
                                    if (entity instanceof org.bukkit.entity.Item) {
                                        org.bukkit.entity.Item item =
                                                (org.bukkit.entity.Item) entity;
                                        ItemStack stack = item.getItemStack();
                                        if (stack != null
                                                && stack.getType()
                                                        .toString()
                                                        .contains("MUSIC_DISC")) {
                                            entity.remove();
                                        }
                                    }
                                });
            }

            jukebox = (Jukebox) jukeboxBlock.getState();
            if (jukebox.getRecord() != null && !jukebox.getRecord().getType().isAir()) {
                jukebox.setRecord(null);
                jukebox.update(true, true);
            }

            for (Player player : jukeboxBlock.getWorld().getPlayers()) {
                if (player.getLocation().distance(jukeboxBlock.getLocation()) <= 200) {
                    List<Sound> musicDiscSounds = new ArrayList<>();
                    for (Sound sound : getSounds()) {
                        String soundKey = sound.toString();
                        if (soundKey.contains("MUSIC_DISC") || soundKey.contains("RECORD")) {
                            musicDiscSounds.add(sound);
                        }
                    }

                    for (Sound sound : musicDiscSounds) {
                        try {
                            player.stopSound(sound);
                        } catch (Exception e) {
                            // Ignore errors
                        }
                    }

                    try {
                        player.stopSound(SoundCategory.RECORDS);
                        player.stopSound(SoundCategory.MUSIC);
                    } catch (Exception e) {
                        // Ignore errors
                    }
                }
            }
        }
    }

    /** Safely plays a music disc to avoid duplication of sounds */
    private static void playMusic(Block jukeboxBlock, Material discMaterial, Player player) {
        if (jukeboxBlock != null && jukeboxBlock.getType() == Material.JUKEBOX) {
            stopMusic(jukeboxBlock);

            Location jbLoc = jukeboxBlock.getLocation();
            jukeboxBlock
                    .getWorld()
                    .getNearbyEntities(jbLoc, 3, 3, 3)
                    .forEach(
                            entity -> {
                                if (entity instanceof org.bukkit.entity.Item) {
                                    org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
                                    ItemStack stack = item.getItemStack();
                                    if (stack != null
                                            && stack.getType().toString().contains("MUSIC_DISC")) {
                                        entity.remove();
                                    }
                                }
                            });

            Jukebox jukebox = (Jukebox) jukeboxBlock.getState();
            jukebox.setRecord(new ItemStack(discMaterial));
            jukebox.update(true, false);

            Sound discSound = getDiscSound(discMaterial);
            if (discSound != null) {
                jukeboxBlock
                        .getWorld()
                        .playSound(jukeboxBlock.getLocation(), discSound, 1.0f, 1.0f);

                playingJukeboxes.put(jukeboxBlock, discSound);

                String discName = getCustomDiscName(discMaterial);
                player.sendMessage("§aNow playing: §e" + discName);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().toString().contains("Jukebox - Select Music")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            UUID playerId = player.getUniqueId();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null) return;

            Block jukeboxBlock = playerJukeboxMap.get(playerId);
            if (jukeboxBlock == null || jukeboxBlock.getType() != Material.JUKEBOX) {
                player.sendMessage("§cJukebox not found. Please try again.");
                return;
            }

            Jukebox jukebox = (Jukebox) jukeboxBlock.getState();

            if (clickedItem.getType() == Material.BARRIER) {
                player.sendMessage("§aStopping music...");

                stopMusic(jukeboxBlock);

                playerDiscPreferences.remove(playerId);
                savePlayerPreference(player, null);

                player.closeInventory();
            } else if (Arrays.asList(MUSIC_DISCS).contains(clickedItem.getType())) {
                Material discMaterial = clickedItem.getType();
                playerDiscPreferences.put(playerId, discMaterial);
                savePlayerPreference(player, discMaterial);

                playMusic(jukeboxBlock, discMaterial, player);

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title().toString().contains("Jukebox - Select Music")) {
            Player player = (Player) event.getPlayer();

            if (!player.getWorld().getName().equals(getAfkWorldName())) {
                playerJukeboxMap.remove(player.getUniqueId());
            }
        }
    }

    /** Save player's disc preference to persistent data */
    private static void savePlayerPreference(Player player, Material discMaterial) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (discMaterial == null) {
            container.remove(jukeboxPreferenceKey);
        } else {
            container.set(jukeboxPreferenceKey, PersistentDataType.STRING, discMaterial.toString());
        }
    }

    /** Load player's disc preference from persistent data */
    public static Material loadPlayerPreference(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (container.has(jukeboxPreferenceKey, PersistentDataType.STRING)) {
            String materialName = container.get(jukeboxPreferenceKey, PersistentDataType.STRING);
            try {
                return Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }

    /** Apply a player's saved music preference to a jukebox */
    public static void applyPlayerPreference(Player player, Jukebox jukebox) {
        Material preference = playerDiscPreferences.get(player.getUniqueId());

        if (preference == null) {
            preference = loadPlayerPreference(player);
        }

        if (preference != null) {
            Block jukeboxBlock = jukebox.getBlock();
            playMusic(jukeboxBlock, preference, player);
        }
    }
}
