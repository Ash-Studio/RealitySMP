package me.ash.realitySMP.listeners;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.jobs.Job;
import me.ash.realitySMP.jobs.JobManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class PoliceListener implements Listener {
    private final RealitySMP plugin;

    public PoliceListener(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        JobManager jobManager = plugin.getJobManager();

        // Check if player is a police officer
        Job playerJob = jobManager.getPlayerJob(player.getUniqueId());
        if (playerJob == null || !playerJob.getName().equalsIgnoreCase("Police Officer")) {
            return;
        }

        // Check if player is holding a baton (stick)
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() != Material.STICK) {
            return;
        }

        // Check if the entity is a player
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getRightClicked();

        // Implement handcuffing mechanic
        player.sendMessage("§9You have detained " + target.getName() + ".");
        target.sendMessage("§cYou have been detained by Officer " + player.getName() + "!");

        // Apply slowness effect for 30 seconds
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                "effect give " + target.getName() + " minecraft:slowness 30 1 true");

        // Give XP to the police officer
        jobManager.handleJobAction(player, "detain");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if a player is attacking another player
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        // Check for nearby police officers
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.equals(attacker) && !p.equals(victim))
                .filter(p -> p.getLocation().distance(attacker.getLocation()) <= 15.0)
                .forEach(officer -> {
                    Job officerJob = plugin.getJobManager().getPlayerJob(officer.getUniqueId());

                    if (officerJob != null && officerJob.getName().equalsIgnoreCase("Police Officer")) {
                        // Alert the police officer
                        officer.sendMessage("§c§lALERT: §r§c" + attacker.getName() + " is attacking " + victim.getName() + " nearby!");

                        // Give the officer an opportunity to gain XP if they intervene
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            // If the attacker is near the officer after 5 seconds, consider it an intervention
                            if (officer.getLocation().distance(attacker.getLocation()) <= 5.0) {
                                plugin.getJobManager().handleJobAction(officer, "intervene");
                                officer.sendMessage("§9You've gained experience for intervening in a fight.");
                            }
                        }, 100L); // 5 seconds = 100 ticks
                    }
                });
    }
}