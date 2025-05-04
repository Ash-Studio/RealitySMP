package me.ash.realitySMP.commands;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.PlayerIdentity;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IdCommand implements CommandExecutor {
    private final RealitySMP plugin;

    public IdCommand(RealitySMP plugin) {
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
            showIdCard(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /id set <firstname|lastname|dob|address> <value>");
                    return true;
                }
                handleSetField(player, args[1].toLowerCase(), args[2]);
                break;
            case "show":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /id show <player>");
                    return true;
                }
                handleShowId(player, args[1]);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /id, /id set, or /id show.");
                break;
        }

        return true;
    }

    private void showIdCard(Player player) {
        PlayerIdentity identity = plugin.getIdentityManager().getPlayerIdentity(player.getUniqueId());

        if (identity == null) {
            player.sendMessage(ChatColor.RED + "You don't have an ID yet. Use /id set to create one.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "======== ID CARD ========");
        player.sendMessage(ChatColor.YELLOW + "Name: " + identity.getFirstName() + " " + identity.getLastName());
        player.sendMessage(ChatColor.YELLOW + "Date of Birth: " + identity.getDateOfBirth());
        player.sendMessage(ChatColor.YELLOW + "Address: " + identity.getAddress());
        player.sendMessage(ChatColor.YELLOW + "Occupation: " + plugin.getJobManager().getPlayerJobName(player.getUniqueId()));
        player.sendMessage(ChatColor.GOLD + "=======================");
    }

    private void handleSetField(Player player, String field, String value) {
        PlayerIdentity identity = plugin.getIdentityManager().getPlayerIdentity(player.getUniqueId());

        if (identity == null) {
            identity = new PlayerIdentity(player.getUniqueId(), "", "", "", "");
        }

        switch (field) {
            case "firstname":
                identity.setFirstName(value);
                player.sendMessage(ChatColor.GREEN + "First name set to: " + value);
                break;
            case "lastname":
                identity.setLastName(value);
                player.sendMessage(ChatColor.GREEN + "Last name set to: " + value);
                break;
            case "dob":
                identity.setDateOfBirth(value);
                player.sendMessage(ChatColor.GREEN + "Date of birth set to: " + value);
                break;
            case "address":
                identity.setAddress(value);
                player.sendMessage(ChatColor.GREEN + "Address set to: " + value);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown field. Use firstname, lastname, dob, or address.");
                return;
        }

        plugin.getIdentityManager().savePlayerIdentity(player.getUniqueId());
    }

    private void handleShowId(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or not online.");
            return;
        }

        // Check if players are close to each other
        if (player.getLocation().distance(target.getLocation()) > 5) {
            player.sendMessage(ChatColor.RED + "You need to be closer to the player to see their ID.");
            return;
        }

        PlayerIdentity identity = plugin.getIdentityManager().getPlayerIdentity(target.getUniqueId());

        if (identity == null) {
            player.sendMessage(ChatColor.RED + "This player doesn't have an ID.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "======== " + target.getName() + "'s ID CARD ========");
        player.sendMessage(ChatColor.YELLOW + "Name: " + identity.getFirstName() + " " + identity.getLastName());
        player.sendMessage(ChatColor.YELLOW + "Date of Birth: " + identity.getDateOfBirth());
        player.sendMessage(ChatColor.YELLOW + "Address: " + identity.getAddress());
        player.sendMessage(ChatColor.YELLOW + "Occupation: " + plugin.getJobManager().getPlayerJobName(target.getUniqueId()));
        player.sendMessage(ChatColor.GOLD + "=======================");
    }
}