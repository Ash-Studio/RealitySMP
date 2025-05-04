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

public class MoodManager {
    private final RealitySMP plugin;
    private final Map<UUID, Integer> playerMood = new HashMap<>();
    private final File moodFile;
    private FileConfiguration moodConfig;

    public MoodManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.moodFile = new File(plugin.getDataFolder(), "mood.yml");
        loadMood();
    }

    public void loadMood() {
        if (!moodFile.exists()) {
            try {
                moodFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create mood.yml file: " + e.getMessage());
            }
        }

        moodConfig = YamlConfiguration.loadConfiguration(moodFile);

        // Load mood values for all players
        if (moodConfig.contains("mood")) {
            for (String uuidString : moodConfig.getConfigurationSection("mood").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int mood = moodConfig.getInt("mood." + uuidString);
                playerMood.put(uuid, mood);
            }
        }
    }

    public void saveMood() {
        // Save all mood values
        for (Map.Entry<UUID, Integer> entry : playerMood.entrySet()) {
            moodConfig.set("mood." + entry.getKey().toString(), entry.getValue());
        }

        try {
            moodConfig.save(moodFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mood.yml file: " + e.getMessage());
        }
    }

    public int getMood(UUID playerUUID) {
        return playerMood.getOrDefault(playerUUID, 100);
    }

    public void setMood(UUID playerUUID, int mood) {
        // Ensure mood is within valid range
        mood = Math.max(0, Math.min(100, mood));
        playerMood.put(playerUUID, mood);

        // Update player effects if they're online
        applyMoodEffects(playerUUID, mood);
    }

    public void increaseMood(UUID playerUUID, int amount) {
        int currentMood = getMood(playerUUID);
        setMood(playerUUID, currentMood + amount);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline() && amount > 0) {
            player.sendMessage("§a§lYour mood improves!");
        }
    }

    public void decreaseMood(UUID playerUUID, int amount) {
        int currentMood = getMood(playerUUID);
        setMood(playerUUID, currentMood - amount);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline() && amount > 0) {
            player.sendMessage("§c§lYour mood worsens.");
        }
    }

    private void applyMoodEffects(UUID playerUUID, int mood) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }

        // Apply effects based on mood level
        if (mood <= 20) {
            // Very low mood - slowness
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:slowness 30 0 true");

            // Visual indicator for depression
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "execute at " + player.getName() + " run particle minecraft:rain ~ ~2 ~ 0.3 0.1 0.3 0.01 2");
        } else if (mood >= 80) {
            // High mood - speed and jump boost
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:speed 30 0 true");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "effect give " + player.getName() + " minecraft:jump_boost 30 0 true");
        }
    }

    public void handleEnvironmentalMoodEffects(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Being outside during good weather improves mood slightly
        if (player.getWorld().hasStorm() || player.getWorld().isThundering()) {
            // Bad weather can decrease mood
            if (Math.random() < 0.1) {
                decreaseMood(playerUUID, 1);
            }
        } else if (player.getLocation().getBlock().getLightFromSky() > 10) {
            // Good weather and sunshine improve mood
            if (Math.random() < 0.1) {
                increaseMood(playerUUID, 1);
            }
        }

        // Being near other players can improve mood
        int nearbyPlayers = 0;
        for (Player otherPlayer : plugin.getServer().getOnlinePlayers()) {
            if (!otherPlayer.equals(player) && otherPlayer.getLocation().distance(player.getLocation()) < 10) {
                nearbyPlayers++;
            }
        }

        if (nearbyPlayers > 0 && Math.random() < 0.1) {
            increaseMood(playerUUID, 1);
        }
    }

    public void improveFromMusic(UUID playerUUID) {
        // Called when a player listens to music (jukebox)
        increaseMood(playerUUID, 5);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§d§lThe music lifts your spirits!");
        }
    }

    public void improveFromFood(UUID playerUUID) {
        // Called when a player eats good food
        increaseMood(playerUUID, 3);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§6§lThe delicious food improves your mood!");
        }
    }

    public void decreaseFromHunger(UUID playerUUID) {
        // Called when a player is hungry
        decreaseMood(playerUUID, 2);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§c§lBeing hungry makes you irritable.");
        }
    }

    public void decreaseFromLackOfSleep(UUID playerUUID) {
        // Called when a player hasn't slept for a long time
        decreaseMood(playerUUID, 2);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§c§lLack of sleep is affecting your mood.");
        }
    }

    public void improveFromRest(UUID playerUUID) {
        // Called when a player sleeps
        increaseMood(playerUUID, 10);

        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§a§lA good night's sleep has improved your mood!");
        }
    }

    public void checkForLowMoodEffects() {
        // Periodically check all online players for low mood effects
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            int mood = getMood(playerUUID);

            if (mood < 30) {
                // Chance for negative effects due to low mood
                if (Math.random() < 0.2) {
                    // Apply weakness effect
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            "effect give " + player.getName() + " minecraft:weakness 60 0 true");
                    player.sendMessage("§c§lYour low mood is affecting your strength.");
                }
            }
        }
    }

    public void improveFromSocializing(UUID playerUUID, UUID otherPlayerUUID) {
        // Called when players interact positively
        increaseMood(playerUUID, 2);
        increaseMood(otherPlayerUUID, 2);

        Player player = plugin.getServer().getPlayer(playerUUID);
        Player otherPlayer = plugin.getServer().getPlayer(otherPlayerUUID);

        if (player != null && player.isOnline()) {
            player.sendMessage("§a§lSocializing with " +
                    (otherPlayer != null ? otherPlayer.getName() : "someone") +
                    " has improved your mood!");
        }

        if (otherPlayer != null && otherPlayer.isOnline()) {
            otherPlayer.sendMessage("§a§lSocializing with " +
                    (player != null ? player.getName() : "someone") +
                    " has improved your mood!");
        }
    }
}