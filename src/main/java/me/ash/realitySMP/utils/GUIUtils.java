package me.ash.realitySMP.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public class GUIUtils implements Listener {
    private static final Map<UUID, String> openInventories = new HashMap<>();
    private static final Map<String, Map<Integer, Consumer<InventoryClickEvent>>> clickHandlers = new HashMap<>();
    private static final Set<String> registeredGUIs = new HashSet<>();
    private static final Map<UUID, Map<String, Object>> playerGUIData = new HashMap<>();

    private static Plugin plugin;

    /**
     * Initialize GUIUtils with the plugin instance
     *
     * @param pluginInstance The plugin instance
     */
    public static void initialize(Plugin pluginInstance) {
        plugin = pluginInstance;
        Bukkit.getPluginManager().registerEvents(new GUIUtils(), plugin);
    }

    /**
     * Create a new GUI inventory
     *
     * @param title The title of the inventory
     * @param size The size of the inventory (must be a multiple of 9)
     * @param guiId A unique identifier for this GUI type
     * @return The created inventory
     */
    public static Inventory createGUI(String title, int size, String guiId) {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Inventory size must be a multiple of 9");
        }

        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
        registeredGUIs.add(guiId);

        if (!clickHandlers.containsKey(guiId)) {
            clickHandlers.put(guiId, new HashMap<>());
        }

        return inventory;
    }

    /**
     * Open a GUI for a player and track it
     *
     * @param player The player
     * @param inventory The inventory to open
     * @param guiId The unique identifier for this GUI type
     */
    public static void openGUI(Player player, Inventory inventory, String guiId) {
        player.openInventory(inventory);
        openInventories.put(player.getUniqueId(), guiId);
    }

    /**
     * Set a click handler for a specific slot in a GUI
     *
     * @param guiId The unique identifier for the GUI type
     * @param slot The inventory slot
     * @param handler The click handler
     */
    public static void setClickHandler(String guiId, int slot, Consumer<InventoryClickEvent> handler) {
        if (!clickHandlers.containsKey(guiId)) {
            clickHandlers.put(guiId, new HashMap<>());
        }

        clickHandlers.get(guiId).put(slot, handler);
    }

    /**
     * Create an item for a GUI
     *
     * @param material The material of the item
     * @param name The display name of the item
     * @param lore The lore of the item
     * @return The created item
     */
    public static ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            if (!loreList.isEmpty()) {
                meta.setLore(loreList);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Create a decorated item for a GUI with formatted lore lines
     *
     * @param material The material of the item
     * @param name The display name of the item
     * @param loreLines The lore lines
     * @return The created item
     */
    public static ItemStack createGuiItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (loreLines != null && !loreLines.isEmpty()) {
                List<String> formattedLore = new ArrayList<>();
                for (String line : loreLines) {
                    formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(formattedLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Fill empty slots in a GUI with a filler item
     *
     * @param inventory The inventory to fill
     * @param fillerItem The filler item
     */
    public static void fillEmptySlots(Inventory inventory, ItemStack fillerItem) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem.clone());
            }
        }
    }

    /**
     * Create a navigation bar at the bottom of a GUI
     *
     * @param inventory The inventory
     * @param currentPage The current page
     * @param totalPages The total number of pages
     * @param prevPageSlot The slot for the previous page button
     * @param nextPageSlot The slot for the next page button
     * @param closeSlot The slot for the close button
     * @param guiId The GUI id for registering handlers
     */
    public static void createNavigationBar(Inventory inventory, int currentPage, int totalPages,
                                           int prevPageSlot, int nextPageSlot, int closeSlot, String guiId) {
        // Previous page button
        if (currentPage > 1) {
            inventory.setItem(prevPageSlot, createGuiItem(Material.ARROW, "&e« Previous Page"));
            setClickHandler(guiId, prevPageSlot, event -> {
                Player player = (Player) event.getWhoClicked();
                navigateToPage(player, guiId, currentPage - 1);
            });
        } else {
            inventory.setItem(prevPageSlot, createGuiItem(Material.BARRIER, "&7« Previous Page", "&8(You are on the first page)"));
        }

        // Next page button
        if (currentPage < totalPages) {
            inventory.setItem(nextPageSlot, createGuiItem(Material.ARROW, "&eNext Page »"));
            setClickHandler(guiId, nextPageSlot, event -> {
                Player player = (Player) event.getWhoClicked();
                navigateToPage(player, guiId, currentPage + 1);
            });
        } else {
            inventory.setItem(nextPageSlot, createGuiItem(Material.BARRIER, "&7Next Page »", "&8(You are on the last page)"));
        }

        // Close button
        inventory.setItem(closeSlot, createGuiItem(Material.BARRIER, "&cClose"));
        setClickHandler(guiId, closeSlot, event -> {
            event.getWhoClicked().closeInventory();
        });
    }

    /**
     * Handle page navigation logic
     *
     * @param player The player
     * @param guiId The GUI ID
     * @param page The page to navigate to
     */
    public static void navigateToPage(Player player, String guiId, int page) {
        // This method should be overridden in specific implementations
        // to handle the actual page navigation logic
    }

    /**
     * Store data for a player's GUI session
     *
     * @param player The player
     * @param key The data key
     * @param value The data value
     */
    public static void setPlayerGUIData(Player player, String key, Object value) {
        UUID playerUUID = player.getUniqueId();

        if (!playerGUIData.containsKey(playerUUID)) {
            playerGUIData.put(playerUUID, new HashMap<>());
        }

        playerGUIData.get(playerUUID).put(key, value);
    }

    /**
     * Get data for a player's GUI session
     *
     * @param player The player
     * @param key The data key
     * @return The data value, or null if not found
     */
    public static Object getPlayerGUIData(Player player, String key) {
        UUID playerUUID = player.getUniqueId();

        if (playerGUIData.containsKey(playerUUID)) {
            return playerGUIData.get(playerUUID).get(key);
        }

        return null;
    }

    /**
     * Clear all data for a player's GUI session
     *
     * @param player The player
     */
    public static void clearPlayerGUIData(Player player) {
        playerGUIData.remove(player.getUniqueId());
    }

    /**
     * Create a standard empty filler item (gray glass pane)
     *
     * @return The filler item
     */
    public static ItemStack createFillerItem() {
        return createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    /**
     * Create a confirmation GUI with Yes/No options
     *
     * @param title The title of the GUI
     * @param message The confirmation message
     * @param onConfirm The action to perform on confirmation
     * @param onCancel The action to perform on cancellation
     * @return The confirmation GUI
     */
    public static Inventory createConfirmationGUI(String title, String message,
                                                  Consumer<Player> onConfirm,
                                                  Consumer<Player> onCancel) {
        String guiId = "confirmation_" + UUID.randomUUID().toString().substring(0, 8);
        Inventory inventory = createGUI(title, 27, guiId);

        // Fill with glass panes
        ItemStack filler = createFillerItem();
        fillEmptySlots(inventory, filler);

        // Message item
        List<String> messageLore = new ArrayList<>();
        for (String line : message.split("\n")) {
            messageLore.add("&f" + line);
        }
        inventory.setItem(4, createGuiItem(Material.PAPER, "&e&lConfirmation", messageLore));

        // Confirm button
        inventory.setItem(11, createGuiItem(Material.LIME_WOOL, "&a&lConfirm", "&7Click to confirm"));
        setClickHandler(guiId, 11, event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            if (onConfirm != null) {
                onConfirm.accept(player);
            }
        });

        // Cancel button
        inventory.setItem(15, createGuiItem(Material.RED_WOOL, "&c&lCancel", "&7Click to cancel"));
        setClickHandler(guiId, 15, event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            if (onCancel != null) {
                onCancel.accept(player);
            }
        });

        return inventory;
    }

    /**
     * Handle inventory clicks for GUI interfaces
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        if (!openInventories.containsKey(playerUUID)) {
            return;
        }

        String guiId = openInventories.get(playerUUID);

        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Execute the click handler if one exists for this slot
        if (clickHandlers.containsKey(guiId)) {
            int slot = event.getRawSlot();
            Map<Integer, Consumer<InventoryClickEvent>> handlers = clickHandlers.get(guiId);

            if (handlers.containsKey(slot)) {
                handlers.get(slot).accept(event);
            }
        }
    }

    /**
     * Clean up when a GUI is closed
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            UUID playerUUID = player.getUniqueId();

            // Handle inventory close logic
            openInventories.remove(playerUUID);
        }
    }

    /**
     * Create a simple paginated menu
     *
     * @param title The title of the GUI
     * @param items The items to display
     * @param page The current page
     * @param guiId The GUI ID
     * @return The paginated inventory
     */
    public static Inventory createPaginatedGUI(String title, List<ItemStack> items, int page, String guiId) {
        // Calculate total pages (items per page = 45, assuming 9*6 inventory with bottom row for navigation)
        final int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        // Create the inventory
        Inventory inventory = createGUI(title + " - Page " + page + "/" + totalPages, 54, guiId);

        // Fill with items for the current page
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            inventory.setItem(slot, items.get(i));
        }

        // Create navigation bar
        createNavigationBar(inventory, page, totalPages, 45, 53, 49, guiId);

        // Fill remaining slots with filler items
        ItemStack filler = createFillerItem();
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        return inventory;
    }

    /**
     * Update a specific slot in a player's open GUI
     *
     * @param player The player
     * @param slot The slot to update
     * @param item The new item
     */
    public static void updateSlot(Player player, int slot, ItemStack item) {
        if (player.getOpenInventory().getTopInventory() != null) {
            player.getOpenInventory().getTopInventory().setItem(slot, item);
        }
    }

    /**
     * Get the current GUI ID for a player
     *
     * @param player The player
     * @return The GUI ID or null if no GUI is open
     */
    public static String getCurrentGUI(Player player) {
        return openInventories.get(player.getUniqueId());
    }
}