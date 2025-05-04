package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JailCommand implements CommandExecutor {

    private final RealitySMP plugin;

    public JailCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player is a police officer
        if (!plugin.getJobManager().hasJob(player.getUniqueId(), "police")) {
            player.sendMessage(ChatColor.RED + "Only police officers can send players to jail!");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /jail <player> <minutes>");
            return false;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        // Check if target is handcuffed
        if (!plugin.getPoliceManager().isHandcuffed(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must handcuff the player before jailing them!");
            return true;
        }

        // Parse jail time
        int minutes;
        try {
            minutes = Integer.parseInt(args[1]);
            if (minutes <= 0 || minutes > 60) {
                player.sendMessage(ChatColor.RED + "Jail time must be between 1 and 60 minutes!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid jail time! Must be a number.");
            return false;
        }

        // Send player to jail
        plugin.getPoliceManager().jailPlayer(target.getUniqueId(), minutes);

        // Uncuff the player since they're in jail now
        plugin.getPoliceManager().setHandcuffed(target.getUniqueId(), false);

        // Decrease wanted level
        plugin.getPoliceManager().decreaseWantedLevel(target.getUniqueId());

        // Send messages
        player.sendMessage(ChatColor.GREEN + "You sent " + target.getName() + " to jail for " + minutes + " minutes.");

        // Broadcast message
        Bukkit.broadcastMessage(
                ChatColor.BLUE + "[Police] " + ChatColor.YELLOW + target.getName() +
                        " has been jailed for " + minutes + " minutes by Officer " + player.getName() + "!"
        );

        return true;
    }
}