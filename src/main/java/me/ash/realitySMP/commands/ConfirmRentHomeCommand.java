package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmRentHomeCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public ConfirmRentHomeCommand(RealitySMP plugin) {
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

        if (pendingAction == null || !pendingAction.startsWith("rent:")) {
            player.sendMessage(ChatColor.RED + "You don't have a pending home rental.");
            return true;
        }

        String apartmentId = pendingAction.substring(5); // Remove "rent:" prefix

        if (!plugin.getHousingManager().isApartmentAvailable(apartmentId)) {
            player.sendMessage(ChatColor.RED + "This apartment is no longer available.");
            plugin.getPendingActions().remove(player.getUniqueId());  // Changed from clear() to remove()
            return true;
        }

        Apartment apartment = plugin.getHousingManager().getApartment(apartmentId);
        double rent = apartment.getRent();

        // Check if player has enough money for first month's rent
        if (plugin.getEconomy().getBalance(player) < rent) {
            player.sendMessage(ChatColor.RED + "You don't have enough money for the first month's rent. You need $" + rent);
            return true;
        }

        // Withdraw money and assign apartment
        plugin.getEconomy().withdrawPlayer(player, rent);
        plugin.getHousingManager().assignHomeToPlayer(player.getUniqueId(), apartmentId, true);

        player.sendMessage(ChatColor.GREEN + "Congratulations! You've rented " + apartment.getName() + " for $" + rent + "/month");
        player.sendMessage(ChatColor.GREEN + "Your rent will be automatically deducted from your bank account each month.");
        player.sendMessage(ChatColor.GREEN + "You can teleport to your home using /home");

        // Clear pending action
        plugin.getPendingActions().remove(player.getUniqueId());  // Changed from clear() to remove()

        return true;
    }
}