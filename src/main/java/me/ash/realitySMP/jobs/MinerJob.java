package me.ash.realitySMP.jobs;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MinerJob extends Job {
    private final Map<Material, Integer> oreExperience;

    public MinerJob(RealitySMP plugin) {
        super(plugin, "Miner", 100.0);

        // Set up ore values
        oreExperience = new HashMap<>();
        oreExperience.put(Material.COAL_ORE, 1);
        oreExperience.put(Material.IRON_ORE, 2);
        oreExperience.put(Material.GOLD_ORE, 3);
        oreExperience.put(Material.LAPIS_ORE, 3);
        oreExperience.put(Material.REDSTONE_ORE, 3);
        oreExperience.put(Material.DIAMOND_ORE, 5);
        oreExperience.put(Material.EMERALD_ORE, 7);
        oreExperience.put(Material.ANCIENT_DEBRIS, 10);
    }

    @Override
    public void onJobJoin(Player player) {
        // Give mining equipment
        giveMiningEquipment(player);

        player.sendMessage("§6You are now a Miner! Mine ores to earn money and experience.");
    }

    @Override
    public void onJobLeave(Player player) {
        player.sendMessage("§6You are no longer a Miner. Your mining tools are still yours to keep.");
    }

    @Override
    public boolean onJobAction(Player player, String action) {
        if (action.startsWith("mine_")) {
            String oreName = action.substring(5).toUpperCase();
            try {
                Material material = Material.valueOf(oreName);
                if (oreExperience.containsKey(material)) {
                    int xp = oreExperience.get(material);
                    addJobExperience(player.getUniqueId(), xp);

                    // Pay the miner directly
                    double pay = xp * 2.0 * (1 + (0.1 * (getJobLevel(player.getUniqueId()) - 1)));
                    plugin.getEconomy().depositPlayer(player, pay);
                    player.sendMessage("§6You earned $" + pay + " and " + xp + " XP for mining " +
                            material.toString().toLowerCase().replace('_', ' '));
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
                // Not a valid material, do nothing
            }
        }
        return false;
    }

    private void giveMiningEquipment(Player player) {
        // Mining pickaxe
        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.setDisplayName("§6Miner's Pickaxe");
        pickaxeMeta.setLore(Arrays.asList("§7Standard issue mining equipment", "§7Issued to: " + player.getName()));
        pickaxe.setItemMeta(pickaxeMeta);

        player.getInventory().addItem(pickaxe);
    }
}