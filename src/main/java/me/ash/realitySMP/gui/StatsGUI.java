package me.ash.realitySMP.gui;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsGUI {
    private final RealitySMP plugin;
    private static final String STATS_TITLE = ChatColor.AQUA + "Player Statistics";
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public StatsGUI(RealitySMP plugin) {
        this.plugin = plugin;
    }

    public void openStats(Player player) {
        openStats(player, player);
    }

    public void openStats(Player viewer, Player target) {
        Inventory stats = Bukkit.createInventory(null, 27, STATS_TITLE);

        // Get player stats
        double bankBalance = plugin.getBankManager().getBankBalance(target.getUniqueId());
        double cashBalance = plugin.getEconomy().getBalance(target);

        // Player head
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(target);
        skullMeta.setDisplayName(ChatColor.GOLD + target.getName() + "'s Profile");
        List<String> headLore = new ArrayList<>();
        headLore.add(ChatColor.GRAY + "UUID: " + target.getUniqueId().toString());
        headLore.add(ChatColor.GRAY + "First Joined: " + new java.util.Date(target.getFirstPlayed()).toString());
        skullMeta.setLore(headLore);
        playerHead.setItemMeta(skullMeta);
        stats.setItem(4, playerHead);

        // Economic information
        stats.setItem(10, createGuiItem(Material.GOLD_INGOT,
                ChatColor.YELLOW + "Economic Information",
                ChatColor.WHITE + "Bank Balance: " + ChatColor.GREEN + "$" + MONEY_FORMAT.format(bankBalance),
                ChatColor.WHITE + "Cash on Hand: " + ChatColor.GREEN + "$" + MONEY_FORMAT.format(cashBalance),
                ChatColor.WHITE + "Total Net Worth: " + ChatColor.GREEN + "$" + MONEY_FORMAT.format(bankBalance + cashBalance)));

        // Player properties
        if (plugin.getHousingManager().ownsProperty(target.getUniqueId())) {
            String propertyInfo = plugin.getHousingManager().getPropertyInfo(target.getUniqueId());
            stats.setItem(12, createGuiItem(Material.DARK_OAK_DOOR,
                    ChatColor.DARK_GREEN + "Property Ownership",
                    ChatColor.WHITE + propertyInfo));
        } else {
            stats.setItem(12, createGuiItem(Material.RED_BED,
                    ChatColor.RED + "No Property Owned",
                    ChatColor.GRAY + "This player doesn't own any properties."));
        }

        // Job information
        stats.setItem(14, createGuiItem(Material.IRON_PICKAXE,
                ChatColor.BLUE + "Job Information",
                ChatColor.WHITE + "Current Job: " + ChatColor.YELLOW + "None",
                ChatColor.WHITE + "Job Level: " + ChatColor.YELLOW + "N/A",
                ChatColor.WHITE + "Job Experience: " + ChatColor.YELLOW + "0/0"));

        // Criminal record
        stats.setItem(16, createGuiItem(Material.IRON_BARS,
                ChatColor.RED + "Criminal Record",
                ChatColor.WHITE + "Wanted Status: " + ChatColor.GREEN + "Clean",
                ChatColor.WHITE + "Crimes Committed: " + ChatColor.GREEN + "0",
                ChatColor.WHITE + "Times Jailed: " + ChatColor.GREEN + "0"));

        viewer.openInventory(stats);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }

    public static boolean isStatsInventory(Inventory inventory) {
        return inventory != null && inventory.getTitle().equals(STATS_TITLE);
    }
}