package me.ash.realitySMP.listeners;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HandcuffListener implements Listener {
    private final RealitySMP plugin;

    public HandcuffListener(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check if player is handcuffed
        if (plugin.getPoliceManager().isHandcuffed(player.getUniqueId())) {
            // Allow looking around but prevent moving
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can't move while handcuffed!");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Prevent handcuffed players from breaking blocks
        if (plugin.getPoliceManager().isHandcuffed(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't break blocks while handcuffed!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Prevent handcuffed players from placing blocks
        if (plugin.getPoliceManager().isHandcuffed(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks while handcuffed!");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Prevent handcuffed players from interacting
        if (plugin.getPoliceManager().isHandcuffed(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't interact with objects while handcuffed!");
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Check if attacker is a handcuffed player
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();

            if (plugin.getPoliceManager().isHandcuffed(attacker.getUniqueId())) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "You can't attack while handcuffed!");
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Prevent handcuffed players from using most commands
        if (plugin.getPoliceManager().isHandcuffed(player.getUniqueId())) {
            String command = event.getMessage().toLowerCase();

            // Allow some basic commands
            if (!command.startsWith("/helpop") && !command.startsWith("/msg") && !command.startsWith("/r")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can't use commands while handcuffed!");
            }
        }
    }
}