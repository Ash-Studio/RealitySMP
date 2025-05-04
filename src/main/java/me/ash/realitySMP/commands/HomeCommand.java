package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final RealitySMP plugin;

    public HomeCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player is jailed
        if (plugin.getPoliceManager().isJailed(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can't go home while in jail!");
            long remainingTime = plugin.getPoliceManager().getRemainingJailTime(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Remaining jail time: " + remainingTime + " seconds.");
            return true;
        }

        // Check if the player has a home
        if (!plugin.getHousingManager().playerHasHome(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't have a home! Buy or rent an apartment first.");
            return true;
        }

        // Get player's apartment
        Apartment apartment = plugin.getHousingManager().getPlayerApartment(player.getUniqueId());

        // Teleport player home
        player.teleport(apartment.getDoorLocation());
        player.sendMessage(ChatColor.GREEN + "Welcome home to " + apartment.getName() + "!");

        return true;
    }
}