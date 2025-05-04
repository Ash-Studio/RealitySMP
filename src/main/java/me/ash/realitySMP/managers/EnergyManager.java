package me.ash.realitySMP.managers;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnergyManager {
    private final RealitySMP plugin;
    private final Map<UUID, Integer> playerEnergy = new HashMap<>();
    private final File energyFile;
    private FileConfiguration energyConfig;

    public EnergyManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.energyFile = new File(plugin.getDataFolder(), "energy.yml");
        loadEnergy();
    }

    public void loadEnergy() {
        if (!energyFile.exists()) {
            try {
                energyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create energy.yml file: " + e.getMessage());
            }
        }

        energyConfig = YamlConfiguration.loadConfiguration(energyFile);

        // Load energy values for all players
        if (energyConfig.contains("energy")) {
            for (String uuidString : energyConfig.getConfigurationSection("energy").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int energy = energyConfig.getInt("energy." + uuidString);
                playerEnergy.put(uuid, energy);
            }
        }
    }

    public void saveEnergy() {
        // Save all energy values
        for (Map.Entry<UUID, Integer> entry : playerEnergy.entrySet()) {
            energyConfig.set("energy." + entry.getKey().toString(), entry.getValue());
        }

        try {
            energyConfig.save(energyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save energy.yml file: " + e.getMessage());
        }
    }

    public int getEnergy(UUID playerUUID) {
        return playerEnergy.getOrDefault(playerUUID, 100);
    }

    public void setEnergy(UUID playerUUID, int energy) {
        // Ensure energy is within valid range
        energy = Math.max(0, Math.min(100, energy));
        playerEnergy.put(playerUUID, energy);

        // Update player effects if they're online
        applyEnergyEffects(playerUUID, energy);
    }

    public void increaseEnergy(UUID playerUUID, int amount) {
        int currentEnergy = getEnergy(playerUUID);
        setEnergy(playerUUID, currentEnergy + amount);
    }

    public void decreaseEnergy(UUID playerUUID, int amount) {
        int currentEnergy = getEnergy(playerUUID);
        setEnergy(playerUUID, currentEnergy - amount);
    }

    public void decreaseEnergyTick() {
        // Called by the daily tick task to decrease energy over time
        for (UUID playerUUID : playerEnergy.keySet()) {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                decreaseEnergy(playerUUID, 1);

                // Notify player if energy is low
                int energy = getEnergy(playerUUID);
                if (energy <= 20 && energy % 5 == 0) {
                    player.sendMessage("§c§lYou are extremely tired! Get some sleep soon.");
                } else if (energy <= 40 && energy % 10 == 0) {
                    player.sendMessage("§eYou are feeling tired. Consider sleeping soon.");
                }
            }
        }
    }

    private void applyEnergyEffects(UUID playerUUID, int energy) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }

        // Apply effects based on energy level
        if (energy <= 10) {
            // Very low energy - strong slowness and mining fatigue
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:slowness 30 2 true");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:mining_fatigue 30 2 true");
        } else if (energy <= 30) {
            // Low energy - mild slowness and mining fatigue
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:slowness 30 0 true");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:mining_fatigue 30 0 true");
        } else if (energy >= 90) {
            // High energy - speed boost
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:speed 30 0 true");
        }
    }

    public void restoreEnergyFromSleep(UUID playerUUID) {
        setEnergy(playerUUID, 100);
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§a§lYou feel refreshed and energized after sleeping!");
        }
    }
}