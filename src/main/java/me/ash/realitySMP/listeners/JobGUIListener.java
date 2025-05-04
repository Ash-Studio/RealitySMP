package me.ash.realitySMP.listeners;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class JobGUIListener implements Listener {
    private final RealitySMP plugin;

    public JobGUIListener(RealitySMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_BLUE + "Job Selection")) {
            event.setCancelled(true); // Prevent taking items

            if (event.getCurrentItem() == null) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();

            // Check if item has a name
            if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) {
                return;
            }

            String clickedName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            // Find job by display name
            Map<String, Job> availableJobs = plugin.getJobManager().getAvailableJobs();
            for (Job job : availableJobs.values()) {
                if (job.getDisplayName().equals(clickedName)) {
                    // Join this job
                    plugin.getJobManager().joinJob(player, job.getName());

                    // Close inventory
                    player.closeInventory();
                    return;
                }
            }
        }
    }
}