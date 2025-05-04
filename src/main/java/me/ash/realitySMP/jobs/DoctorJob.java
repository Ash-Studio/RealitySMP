package me.ash.realitySMP.jobs;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DoctorJob extends Job {
    private final Map<UUID, Long> healCooldowns;
    private static final int HEAL_COOLDOWN_SECONDS = 60;

    public DoctorJob(RealitySMP plugin) {
        super(plugin, "Doctor", 130.0);
        this.healCooldowns = new HashMap<>();
    }

    @Override
    public void onJobJoin(Player player) {
        // Give medical equipment
        giveMedicalEquipment(player);

        player.sendMessage("§cYou are now a Doctor! Help heal injured players.");
    }

    @Override
    public void onJobLeave(Player player) {
        player.sendMessage("§cYou are no longer a Doctor.");
    }

    @Override
    public boolean onJobAction(Player player, String action) {
        if (action.equals("heal")) {
            // Check cooldown
            long currentTime = System.currentTimeMillis();
            long lastHealTime = healCooldowns.getOrDefault(player.getUniqueId(), 0L);

            if (currentTime - lastHealTime < HEAL_COOLDOWN_SECONDS * 1000) {
                player.sendMessage("§cYou must wait " +
                        ((HEAL_COOLDOWN_SECONDS * 1000 - (currentTime - lastHealTime)) / 1000) +
                        " seconds before healing again!");
                return false;
            }

            // Apply healing to nearby players
            int healRange = 5 + getJobLevel(player.getUniqueId());
            int healAmount = 6 + (getJobLevel(player.getUniqueId()) * 2);
            boolean healedSomeone = false;

            for (Player nearbyPlayer : player.getWorld().getPlayers()) {
                if (nearbyPlayer.getLocation().distance(player.getLocation()) <= healRange &&
                        nearbyPlayer.getHealth() < nearbyPlayer.getMaxHealth()) {

                    double newHealth = Math.min(nearbyPlayer.getHealth() + healAmount, nearbyPlayer.getMaxHealth());
                    nearbyPlayer.setHealth(newHealth);
                    nearbyPlayer.sendMessage("§cDoctor " + player.getName() + " has healed you!");

                    // Remove negative effects
                    nearbyPlayer.removePotionEffect(PotionEffectType.POISON);
                    nearbyPlayer.removePotionEffect(PotionEffectType.WITHER);

                    healedSomeone = true;
                }
            }

            if (healedSomeone) {
                player.sendMessage("§cYou have healed nearby players!");
                addJobExperience(player.getUniqueId(), 10);
                healCooldowns.put(player.getUniqueId(), currentTime);
                return true;
            } else {
                player.sendMessage("§cThere are no injured players nearby to heal.");
                return false;
            }
        }
        return false;
    }

    private void giveMedicalEquipment(Player player) {
        // Medical kit
        ItemStack medkit = new ItemStack(Material.GOLDEN_APPLE, 3);
        ItemMeta medkitMeta = medkit.getItemMeta();
        medkitMeta.setDisplayName("§cMedical Kit");
        medkitMeta.setLore(Arrays.asList("§7Medical supplies", "§7Right-click to heal yourself"));
        medkit.setItemMeta(medkitMeta);

        // Doctor's coat
        ItemStack coat = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta coatMeta = coat.getItemMeta();
        coatMeta.setDisplayName("§cDoctor's Coat");
        coatMeta.setLore(Arrays.asList("§7Official medical attire", "§7Issued to: " + player.getName()));
        coat.setItemMeta(coatMeta);

        player.getInventory().addItem(medkit, coat);
    }
}