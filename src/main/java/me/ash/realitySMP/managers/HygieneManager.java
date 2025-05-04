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

public class HygieneManager {
    private final RealitySMP plugin;
    private final Map<UUID, Integer> playerHygiene = new HashMap<>();
    private final File hygieneFile;
    private FileConfiguration hygieneConfig;

    public HygieneManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.hygieneFile = new File(plugin.getDataFolder(), "hygiene.yml");
        loadHygiene();
    }

    public void loadHygiene() {
        if (!hygieneFile.exists()) {
            try {
                hygieneFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create hygiene.yml file: " + e.getMessage());
            }
        }

        hygieneConfig = YamlConfiguration.loadConfiguration(hygieneFile);

        // Load hygiene values for all players
        if (hygieneConfig.contains("hygiene")) {
            for (String uuidString : hygieneConfig.getConfigurationSection("hygiene").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int hygiene = hygieneConfig.getInt("hygiene." + uuidString);
                playerHygiene.put(uuid, hygiene);
            }
        }
    }

    public void saveHygiene() {
        // Save all hygiene values
        for (Map.Entry<UUID, Integer> entry : playerHygiene.entrySet()) {
            hygieneConfig.set("hygiene." + entry.getKey().toString(), entry.getValue());
        }

        try {
            hygieneConfig.save(hygieneFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save hygiene.yml file: " + e.getMessage());
        }
    }

    public int getHygiene(UUID playerUUID) {
        return playerHygiene.getOrDefault(playerUUID, 100);
    }

    public void setHygiene(UUID playerUUID, int hygiene) {
        // Ensure hygiene is within valid range
        hygiene = Math.max(0, Math.min(100, hygiene));
        playerHygiene.put(playerUUID, hygiene);

        // Update player effects if they're online
        applyHygieneEffects(playerUUID, hygiene);
    }

    public void increaseHygiene(UUID playerUUID, int amount) {
        int currentHygiene = getHygiene(playerUUID);
        setHygiene(playerUUID, currentHygiene + amount);
    }

    public void decreaseHygiene(UUID playerUUID, int amount) {
        int currentHygiene = getHygiene(playerUUID);
        setHygiene(playerUUID, currentHygiene - amount);
    }

    public void decreaseHygieneTick() {
        // Called by the daily tick task to decrease hygiene over time
        for (UUID playerUUID : playerHygiene.keySet()) {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                decreaseHygiene(playerUUID, 1);

                // Notify player if hygiene is low
                int hygiene = getHygiene(playerUUID);
                if (hygiene <= 20 && hygiene % 5 == 0) {
                    player.sendMessage("§c§lYou smell terrible! Take a bath soon.");
                } else if (hygiene <= 40 && hygiene % 10 == 0) {
                    player.sendMessage("§eYou're starting to smell. Consider washing soon.");
                }
            }
        }
    }

    private void applyHygieneEffects(UUID playerUUID, int hygiene) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }

        // Apply effects based on hygiene level
        if (hygiene <= 20) {
            // Very low hygiene - social penalties
            // Other players might see particles around this player
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "execute at " + player.getName() + " run particle minecraft:smoke ~ ~1 ~ 0.2 0.5 0.2 0.01 1");

            // Decrease mood due to bad hygiene
            plugin.getMoodManager().decreaseMood(playerUUID, 1);
        }
    }

    public void washInWater(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Check if player is in water
        if (player.isInWater()) {
            int currentHygiene = getHygiene(playerUUID);

            // Only increase if not already at max
            if (currentHygiene < 100) {
                increaseHygiene(playerUUID, 5);
                player.sendMessage("§b§lYou feel cleaner after washing in the water!");
            }
        }
    }
}