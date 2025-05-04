package me.ash.realitySMP.gui;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.PhoneApplication;
import me.ash.realitySMP.utils.GuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhoneGUI implements Listener {
    private final RealitySMP plugin;
    private final Player player;
    private final Inventory gui;
    private final List<PhoneApplication> apps = new ArrayList<>();

    public PhoneGUI(RealitySMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "📱 Phone");

        // Register this as a listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Initialize phone apps
        initializeApps();

        // Build the GUI
        buildGui();
    }

    private void initializeApps() {
        // Add all available apps
        apps.add(new PhoneApplication("Jobs", Material.IRON_PICKAXE, 10, player -> {
            player.closeInventory();
            plugin.getJobManager().openJobGUI(player);
        }));

        apps.add(new PhoneApplication("Stats", Material.EXPERIENCE_BOTTLE, 11, player -> {
            player.closeInventory();
            new StatsGUI(plugin, player).open();
        }));

        apps.add(new PhoneApplication("Bank", Material.GOLD_INGOT, 12, player -> {
            player.closeInventory();
            new ATMGUI(plugin, player).open();
        }));

        apps.add(new PhoneApplication("Phonebook", Material.BOOK, 13, player -> {
            player.closeInventory();
            showPlayerList();
        }));

        apps.add(new PhoneApplication("Wanted", Material.IRON_BARS, 14, player -> {
            player.closeInventory();
            showWantedStatus();
        }));

        apps.add(new PhoneApplication("Home", Material.RED_BED, 15, player -> {
            player.closeInventory();
            showHomeInfo();
        }));

        apps.add(new PhoneApplication("ID", Material.NAME_TAG, 16, player -> {
            player.closeInventory();
            Bukkit.dispatchCommand(player, "id");
        }));
    }

    private void buildGui() {
        // Clear inventory
        gui.clear();

        // Add phone border/background
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, GuiUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        // Add apps
        for (PhoneApplication app : apps) {
            ItemStack appItem = new ItemStack(app.getIcon());
            ItemMeta meta = appItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + app.getName());
            appItem.setItemMeta(meta);

            gui.setItem(app.getSlot(), appItem);
        }
    }

    private void showPlayerList() {
        player.sendMessage(ChatColor.GOLD + "===== Online Players =====");
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            UUID playerUUID = onlinePlayer.getUniqueId();
            String job = "Unemployed";
            if (plugin.getJobManager().getPlayerJob(playerUUID) != null) {
                job = plugin.getJobManager().getPlayerJob(playerUUID).getDisplayName();
            }

            player.sendMessage(ChatColor.YELLOW + onlinePlayer.getName() + ChatColor.GRAY + " - " + ChatColor.AQUA + job);
        }
    }

    private void showWantedStatus() {
        int wantedLevel = plugin.getPoliceManager().getWantedLevel(player.getUniqueId());
        String stars = "";

        for (int i = 0; i < 5; i++) {
            if (i < wantedLevel) {
                stars += "★";
            } else {
                stars += "☆";
            }
        }

        player.sendMessage(ChatColor.GOLD + "===== Wanted Status =====");
        player.sendMessage(ChatColor.YELLOW + "Wanted Level: " + ChatColor.RED + stars);

        if (wantedLevel > 0) {
            player.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.RED + "WANTED");
            player.sendMessage(ChatColor.YELLOW + "Caution: Police may arrest you on sight!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.GREEN + "CLEAN");
        }
    }

    private void showHomeInfo() {
        if (plugin.getHousingManager().hasHome(player.getUniqueId())) {
            String homeName = plugin.getHousingManager().getPlayerHome(player.getUniqueId()).getName();
            boolean isRenting = plugin.getHousingManager().isRenting(player.getUniqueId());
            double rentCost = plugin.getHousingManager().getRentCost(player.getUniqueId());

            player.sendMessage(ChatColor.GOLD + "===== Your Home =====");
            player.sendMessage(ChatColor.YELLOW + "Property: " + ChatColor.GREEN + homeName);
            player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.GREEN + (isRenting ? "Renting" : "Owned"));

            if (isRenting) {
                player.sendMessage(ChatColor.YELLOW + "Rent: " + ChatColor.GREEN + "$" + rentCost + " per day");
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "===== Housing =====");
            player.sendMessage(ChatColor.RED + "You don't have a home yet!");
            player.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GREEN + "/buyhome" + ChatColor.YELLOW + " or " +
                    ChatColor.GREEN + "/renthome" + ChatColor.YELLOW + " to get a place to live.");
        }
    }

    public void open() {
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(gui)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Find the app that was clicked
        for (PhoneApplication app : apps) {
            if (app.getSlot() == slot) {
                app.getClickAction().accept(player);
                break;
            }
        }
    }
}