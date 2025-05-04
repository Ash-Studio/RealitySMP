package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final RealitySMP plugin;

    public PayCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return false;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        // Don't allow paying yourself
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You can't pay yourself!");
            return true;
        }

        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount! Please enter a valid number.");
            return false;
        }

        // Transfer the money
        boolean success = plugin.getBankManager().transferMoney(
                player.getUniqueId(), target.getUniqueId(), amount);

        if (success) {
            player.sendMessage(ChatColor.GREEN + "You paid " + ChatColor.GOLD + "$" +
                    String.format("%.2f", amount) + ChatColor.GREEN + " to " + target.getName() + ".");

            target.sendMessage(ChatColor.GREEN + "You received " + ChatColor.GOLD + "$" +
                    String.format("%.2f", amount) + ChatColor.GREEN + " from " + player.getName() + ".");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough money for this transaction!");
        }

        return true;
    }
}