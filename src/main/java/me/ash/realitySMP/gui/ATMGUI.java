package me.ash.realitySMP.gui;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ATMGUI {
    private final RealitySMP plugin;
    private static final String ATM_TITLE = ChatColor.DARK_GREEN + "Bank ATM";
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public ATMGUI(RealitySMP plugin) {
        this.plugin = plugin;
    }

    public void openATM(Player player) {
        Inventory atm = Bukkit.createInventory(null, 27, ATM_TITLE);

        // Get player bank balance
        double bankBalance = plugin.getBankManager().getBankBalance(player.getUniqueId());
        double handBalance = plugin.getEconomy().getBalance(player);

        // Balance information
        ItemStack balanceInfo = createGuiItem(Material.GOLD_BLOCK,
                ChatColor.YELLOW + "Account Information",
                ChatColor.WHITE + "Bank Balance: " + ChatColor.GREEN + "$" + MONEY_FORMAT.format(bankBalance),
                ChatColor.WHITE + "Cash on Hand: " + ChatColor.GREEN + "$" + MONEY_FORMAT.format(handBalance));
        atm.setItem(4, balanceInfo);

        // Deposit options
        atm.setItem(11, createGuiItem(Material.HOPPER, ChatColor.GREEN + "Deposit $100"));
        atm.setItem(12, createGuiItem(Material.HOPPER, ChatColor.GREEN + "Deposit $500"));
        atm.setItem(13, createGuiItem(Material.HOPPER, ChatColor.GREEN + "Deposit $1,000"));
        atm.setItem(14, createGuiItem(Material.HOPPER, ChatColor.GREEN + "Deposit $5,000"));
        atm.setItem(15, createGuiItem(Material.HOPPER, ChatColor.GREEN + "Deposit All"));

        // Withdraw options
        atm.setItem(20, createGuiItem(Material.DISPENSER, ChatColor.RED + "Withdraw $100"));
        atm.setItem(21, createGuiItem(Material.DISPENSER, ChatColor.RED + "Withdraw $500"));
        atm.setItem(22, createGuiItem(Material.DISPENSER, ChatColor.RED + "Withdraw $1,000"));
        atm.setItem(23, createGuiItem(Material.DISPENSER, ChatColor.RED + "Withdraw $5,000"));
        atm.setItem(24, createGuiItem(Material.DISPENSER, ChatColor.RED + "Withdraw All"));

        player.openInventory(atm);
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

    public static boolean isATMInventory(Inventory inventory) {
        return inventory != null && inventory.getTitle().equals(ATM_TITLE);
    }

    public void handleATMClick(Player player, String itemName) {
        if (itemName.contains("Deposit $100")) {
            handleDeposit(player, 100);
        } else if (itemName.contains("Deposit $500")) {
            handleDeposit(player, 500);
        } else if (itemName.contains("Deposit $1,000")) {
            handleDeposit(player, 1000);
        } else if (itemName.contains("Deposit $5,000")) {
            handleDeposit(player, 5000);
        } else if (itemName.contains("Deposit All")) {
            handleDeposit(player, plugin.getEconomy().getBalance(player));
        } else if (itemName.contains("Withdraw $100")) {
            handleWithdraw(player, 100);
        } else if (itemName.contains("Withdraw $500")) {
            handleWithdraw(player, 500);
        } else if (itemName.contains("Withdraw $1,000")) {
            handleWithdraw(player, 1000);
        } else if (itemName.contains("Withdraw $5,000")) {
            handleWithdraw(player, 5000);
        } else if (itemName.contains("Withdraw All")) {
            handleWithdraw(player, plugin.getBankManager().getBankBalance(player.getUniqueId()));
        }

        // Update the inventory
        openATM(player);
    }

    private void handleDeposit(Player player, double amount) {
        if (plugin.getEconomy().getBalance(player) < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough money on hand to deposit this amount.");
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, amount);
        plugin.getBankManager().addToBank(player.getUniqueId(), amount);

        player.sendMessage(ChatColor.GREEN + "Successfully deposited $" + MONEY_FORMAT.format(amount) + " into your bank account.");
    }

    private void handleWithdraw(Player player, double amount) {
        if (!plugin.getBankManager().withdrawFromBank(player.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money in your bank account.");
            return;
        }

        plugin.getEconomy().depositPlayer(player, amount);
        player.sendMessage(ChatColor.GREEN + "Successfully withdrew $" + MONEY_FORMAT.format(amount) + " from your bank account.");
    }
}