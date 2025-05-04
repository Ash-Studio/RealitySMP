package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConfirmSellHomeCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public ConfirmSellHomeCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String pendingAction = plugin.getPendingActions().get(player.getUniqueId());

        if (pendingAction == null || !pendingAction.startsWith("sell:")) {
            player.sendMessage(ChatColor.RED + "You don't have a pending home sale.");
            return true;
        }

        String apartmentId = pendingAction.substring(5); // Remove "sell:" prefix

        if (!plugin.getHousingManager().playerOwnsHome(player.getUniqueId(), apartmentId)) {
            player.sendMessage(ChatColor.RED + "You don't own this home anymore.");
            plugin.getPendingActions().remove(player.getUniqueId());
            return true;
        }

        Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);
        double sellPrice = apartment.getPrice() * 0.7; // 70% of original price

        // Give money and remove apartment
        plugin.getEconomy().depositPlayer(player, sellPrice);
        plugin.getHousingManager().removePlayerHome(player.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "You've sold " + apartment.getName() + " for $" + sellPrice);

        // Clear pending action
        plugin.getPendingActions().remove(player.getUniqueId());

        return true;
    }
}