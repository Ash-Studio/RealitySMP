package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ATMCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public ATMCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /atm <deposit|withdraw> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "You must specify an amount.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
            return true;
        }

        switch (action) {
            case "deposit":
                handleDeposit(player, amount);
                break;
            case "withdraw":
                handleWithdraw(player, amount);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Usage: /atm <deposit|withdraw> <amount>");
                break;
        }

        return true;
    }

    private void handleDeposit(Player player, double amount) {
        if (plugin.getEconomy().getBalance(player) < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough money on hand to deposit this amount.");
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, amount);
        plugin.getBankManager().addToBank(player.getUniqueId(), amount);

        player.sendMessage(ChatColor.GREEN + "Successfully deposited $" + amount + " into your bank account.");
    }

    private void handleWithdraw(Player player, double amount) {
        if (!plugin.getBankManager().withdrawFromBank(player.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money in your bank account.");
            return;
        }

        plugin.getEconomy().depositPlayer(player, amount);
        player.sendMessage(ChatColor.GREEN + "Successfully withdrew $" + amount + " from your bank account.");
    }
}