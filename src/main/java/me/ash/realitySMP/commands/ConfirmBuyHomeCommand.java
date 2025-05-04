package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmBuyHomeCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public ConfirmBuyHomeCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String pendingAction = plugin.getPendingActions().get(player.getUniqueId());

        if (pendingAction == null || !pendingAction.startsWith("buy:")) {
            player.sendMessage(ChatColor.RED + "You don't have a pending home purchase.");
            return true;
        }

        String apartmentId = pendingAction.substring(4); // Remove "buy:" prefix

        if (!plugin.getHousingManager().isApartmentAvailable(apartmentId)) {
            player.sendMessage(ChatColor.RED + "This apartment is no longer available.");
            plugin.getPendingActions().remove(player.getUniqueId());
            return true;
        }

        Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);
        double price = apartment.getPrice();

        // Check if player has enough money
        if (plugin.getEconomy().getBalance(player) < price) {
            player.sendMessage(ChatColor.RED + "You don't have enough money. You need $" + price);
            return true;
        }

        // Withdraw money and assign apartment
        plugin.getEconomy().withdrawPlayer(player, price);
        plugin.getHousingManager().assignHomeToPlayer(player.getUniqueId(), apartmentId, false);

        player.sendMessage(ChatColor.GREEN + "Congratulations! You've purchased " + apartment.getName() + " for $" + price);
        player.sendMessage(ChatColor.GREEN + "You can teleport to your home using /home");

        // Clear pending action
        plugin.getPendingActions().remove(player.getUniqueId());

        return true;
    }
}