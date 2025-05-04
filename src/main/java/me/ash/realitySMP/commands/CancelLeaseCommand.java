package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelLeaseCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public CancelLeaseCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getHousingManager().playerHasHome(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't have a home lease to cancel.");
            return true;
        }

        String apartmentId = plugin.getHousingManager().getPlayerHomeId(player.getUniqueId());
        boolean isRenting = plugin.getHousingManager().isPlayerRenting(player.getUniqueId());

        if (!isRenting) {
            player.sendMessage(ChatColor.RED + "You own this home. Use /sellhome instead.");
            return true;
        }

        Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);

        // Cancel lease and refund security deposit if applicable
        plugin.getHousingManager().removePlayerHome(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "You've canceled your lease for " + apartment.getName());
        player.sendMessage(ChatColor.GREEN + "You will no longer be charged monthly rent.");

        return true;
    }
}