package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.gui.PhoneGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PhoneCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public PhoneCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Check if player has a phone
        if (!plugin.getPhoneManager().hasPhone(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't have a phone. Purchase one at the electronics store!");
            return true;
        }

        // Open phone GUI
        new PhoneGUI(plugin, player).openMainMenu();

        return true;
    }
}