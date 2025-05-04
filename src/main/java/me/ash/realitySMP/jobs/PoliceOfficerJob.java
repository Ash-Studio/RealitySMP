package me.ash.realitySMP.jobs;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PoliceOfficerJob extends Job {

    public PoliceOfficerJob(RealitySMP plugin) {
        super(plugin, "Police Officer", 150.0);
    }

    @Override
    public void onJobJoin(Player player) {
        // Give police equipment
        givePoliceEquipment(player);

        // Send a message to the server
        plugin.getServer().broadcastMessage("§9[Police] §f" + player.getName() + " has joined the police force!");
    }

    @Override
    public void onJobLeave(Player player) {
        // Remove police equipment
        removePoliceEquipment(player);

        plugin.getServer().broadcastMessage("§9[Police] §f" + player.getName() + " has left the police force.");
    }

    @Override
    public boolean onJobAction(Player player, String action) {
        if (action.equalsIgnoreCase("arrest")) {
            // Handle arrest action - this would be triggered by a command or listener
            player.sendMessage("§9You have successfully made an arrest! +10 experience");
            addJobExperience(player.getUniqueId(), 10);
            return true;
        } else if (action.equalsIgnoreCase("ticket")) {
            // Handle issuing a ticket
            player.sendMessage("§9You have issued a ticket! +5 experience");
            addJobExperience(player.getUniqueId(), 5);
            return true;
        }
        return false;
    }

    private void givePoliceEquipment(Player player) {
        // Police baton (stick)
        ItemStack baton = new ItemStack(Material.STICK);
        ItemMeta batonMeta = baton.getItemMeta();
        batonMeta.setDisplayName("§9Police Baton");
        batonMeta.setLore(Arrays.asList("§7Official police equipment", "§7Issued to: " + player.getName()));
        baton.setItemMeta(batonMeta);

        // Handcuffs (lead)
        ItemStack handcuffs = new ItemStack(Material.LEAD);
        ItemMeta handcuffsMeta = handcuffs.getItemMeta();
        handcuffsMeta.setDisplayName("§9Handcuffs");
        handcuffsMeta.setLore(Arrays.asList("§7Official police equipment", "§7Issued to: " + player.getName()));
        handcuffs.setItemMeta(handcuffsMeta);

        player.getInventory().addItem(baton, handcuffs);
    }

    private void removePoliceEquipment(Player player) {
        // Remove police items from inventory
        player.getInventory().remove(Material.STICK);
        player.getInventory().remove(Material.LEAD);
    }
}