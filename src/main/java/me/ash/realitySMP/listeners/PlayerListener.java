package me.ash.realitySMP.listeners;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {
    private final RealitySMP plugin;

    public PlayerListener(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load or create player data
        PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());

        // Welcome message
        if (!player.hasPlayedBefore()) {
            player.sendMessage("§6Welcome to §lReality SMP§r§6!");
            player.sendMessage("§eThis server features jobs, needs, and a realistic economy.");
            player.sendMessage("§eUse §6/job§e to choose your profession and §6/mystats§e to view your needs.");

            // Set default stats for new players
            plugin.getEnergyManager().setEnergy(player.getUniqueId(), 100);
            plugin.getHygieneManager().setHygiene(player.getUniqueId(), 100);
            plugin.getMoodManager().setMood(player.getUniqueId(), 100);
        } else {
            player.sendMessage("§6Welcome back to §lReality SMP§r§6!");
        }

        // Update display name with job title if they have one
        plugin.getJobManager().updatePlayerDisplayName(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save player data when they quit
        plugin.getPlayerManager().savePlayerData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Reduce stats on death
        plugin.getEnergyManager().setEnergy(player.getUniqueId(), 20);
        plugin.getMoodManager().decreaseMood(player.getUniqueId(), 30);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Send message about decreased stats
        player.sendMessage("§cYou feel weak and tired after respawning.");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only process if actual movement occurred (not just head rotation)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Decrease energy slightly when moving (1 in 200 chance to avoid excessive processing)
        if (Math.random() < 0.005) {
            plugin.getEnergyManager().decreaseEnergy(player.getUniqueId(), 1);
        }

        // Decrease hygiene when sprinting (1 in 100 chance)
        if (player.isSprinting() && Math.random() < 0.01) {
            plugin.getHygieneManager().decreaseHygiene(player.getUniqueId(), 1);
        }
    }
}