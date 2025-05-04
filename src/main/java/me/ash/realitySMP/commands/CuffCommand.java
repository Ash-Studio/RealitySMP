package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CuffCommand implements CommandExecutor {

    private final RealitySMP plugin;

    public CuffCommand(RealitySMP plugin) {
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
            player.sendMessage(ChatColor.RED + "Only police officers can use handcuffs!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /cuff <player>");
            return false;
        }

        // Get target player
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        // Check if target is already handcuffed
        boolean isCuffed = plugin.getPoliceManager().isHandcuffed(target.getUniqueId());

        if (isCuffed) {
            // Uncuff the player
            plugin.getPoliceManager().setHandcuffed(target.getUniqueId(), false);
            player.sendMessage(ChatColor.GREEN + "You have uncuffed " + target.getName() + ".");
        } else {
            // Check if player is within range (3 blocks)
            if (player.getLocation().distance(target.getLocation()) > 3) {
                player.sendMessage(ChatColor.RED + "You must be closer to handcuff this player!");
                return true;
            }

            // Handcuff the player
            plugin.getPoliceManager().setHandcuffed(target.getUniqueId(), true);
            player.sendMessage(ChatColor.GREEN + "You have handcuffed " + target.getName() + ".");

            // Broadcast message
            Bukkit.broadcastMessage(
                    ChatColor.BLUE + "[Police] " + ChatColor.YELLOW + target.getName() +
                            " has been handcuffed by Officer " + player.getName() + "!"
            );
        }

        return true;
    }
}