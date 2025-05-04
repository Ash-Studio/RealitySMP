package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RentHomeCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public RentHomeCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /renthome <apartment-id>");
            listAvailableApartments(player);
            return true;
        }

        String apartmentId = args[0];

        if (!plugin.getHousingManager().isApartmentAvailable(apartmentId)) {
            player.sendMessage(ChatColor.RED + "This apartment is not available or does not exist.");
            listAvailableApartments(player);
            return true;
        }

        if (plugin.getHousingManager().playerHasHome(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already own or rent a home. Use /sellhome or /cancellease first.");
            return true;
        }

        Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);
        double rent = apartment.getRent();

        player.sendMessage(ChatColor.YELLOW + "You are about to rent " + apartment.getName() + " for $" + rent + "/month");
        player.sendMessage(ChatColor.YELLOW + "Type /confirmrenthome to confirm your rental.");

        // Store the player's intent to rent this apartment
        plugin.getPendingActions().put(player.getUniqueId(), "rent:" + apartmentId);

        return true;
    }

    private void listAvailableApartments(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Available apartments:");

        for (String apartmentId : plugin.getHousingManager().getApartmentIds()) {
            if (plugin.getHousingManager().isApartmentAvailable(apartmentId)) {
                Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);
                player.sendMessage(ChatColor.YELLOW + "- " + apartment.getName() + " (ID: " + apartmentId + ") - $" + apartment.getPrice() + " or $" + apartment.getRent() + "/month");
            }
        }
    }
}