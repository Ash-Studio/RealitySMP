package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.gui.StatsGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MyStatsCommand implements CommandExecutor {

    private final RealitySMP plugin;

    public MyStatsCommand(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (args.length == 0) {
            // Open stats GUI
            new StatsGUI(plugin, player).open();
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("text")) {
            // Show stats in text format
            int mood = plugin.getMoodManager().getMood(playerUUID);
            int hygiene = plugin.getHygieneManager().getHygiene(playerUUID);
            int energy = plugin.getEnergyManager().getEnergy(playerUUID);
            double wallet = plugin.getBankManager().getWalletBalance(playerUUID);
            double bank = plugin.getBankManager().getBankBalance(playerUUID);

            Job job = plugin.getJobManager().getPlayerJob(playerUUID);
            String jobName = (job != null) ? job.getDisplayName() : "Unemployed";

            player.sendMessage(ChatColor.GOLD + "===== Your Stats =====");
            player.sendMessage(ChatColor.YELLOW + "Mood: " + getColorForValue(mood) + mood + "%");
            player.sendMessage(ChatColor.YELLOW + "Hygiene: " + getColorForValue(hygiene) + hygiene + "%");
            player.sendMessage(ChatColor.YELLOW + "Energy: " + getColorForValue(energy) + energy + "%");
            player.sendMessage(ChatColor.YELLOW + "Wallet: " + ChatColor.GREEN + "$" + wallet);
            player.sendMessage(ChatColor.YELLOW + "Bank: " + ChatColor.GREEN + "$" + bank);
            player.sendMessage(ChatColor.YELLOW + "Job: " + ChatColor.AQUA + jobName);

            return true;
        }

        return false;
    }

    private ChatColor getColorForValue(int value) {
        if (value < 20) {
            return ChatColor.RED;
        } else if (value < 50) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.GREEN;
        }
    }
}