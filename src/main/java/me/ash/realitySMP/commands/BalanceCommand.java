package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final RealitySMP plugin;

    public BalanceCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Check own balance
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can check their own balance!");
                return true;
            }

            Player player = (Player) sender;
            double balance = plugin.getBankManager().getBalance(player.getUniqueId());

            player.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.GOLD + "$" + String.format("%.2f", balance));
            return true;
        } else if (args.length == 1) {
            // Check another player's balance (require permission)
            if (!sender.hasPermission("realitysmp.balance.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to check others' balance!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }

            double balance = plugin.getBankManager().getBalance(target.getUniqueId());

            sender.sendMessage(ChatColor.GREEN + target.getName() + "'s balance: " +
                    ChatColor.GOLD + "$" + String.format("%.2f", balance));
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /balance [player]");
        return false;
    }
}