package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellHomeCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public SellHomeCommand(RealitySMP plugin) {
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
            player.sendMessage(ChatColor.RED + "You don't own a home to sell.");
            return true;
        }

        String apartmentId = plugin.getHousingManager().getPlayerHomeId(player.getUniqueId());
        boolean isRenting = plugin.getHousingManager().isPlayerRenting(player.getUniqueId());

        if (isRenting) {
            player.sendMessage(ChatColor.RED + "You are renting this home. Use /cancellease instead.");
            return true;
        }

        Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);
        double sellPrice = apartment.getPrice() * 0.7; // 70% of original price

        player.sendMessage(ChatColor.YELLOW + "You are about to sell " + apartment.getName() + " for $" + sellPrice);
        player.sendMessage(ChatColor.YELLOW + "Type /confirmsellhome to confirm your sale.");

        // Store the player's intent to sell their home
        plugin.getPendingActions().put(player.getUniqueId(), "sell:" + apartmentId);

        return true;
    }
}