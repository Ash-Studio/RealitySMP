package me.ash.realitySMP.managers;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PoliceManager {
    private final RealitySMP plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;

    // Player data maps
    private final Map<UUID, Integer> wantedLevels = new HashMap<>();
    private final Map<UUID, Boolean> handcuffed = new HashMap<>();
    private final Map<UUID, Long> jailTimes = new HashMap<>();
    private final Map<UUID, BukkitTask> jailTasks = new HashMap<>();

    // Jail location
    private Location jailLocation;

    public PoliceManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "police_data.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load the jail location from config
        loadJailLocation();

        // Load player data
        loadData();
    }

    private void loadJailLocation() {
        FileConfiguration config = plugin.getConfig();
        String worldName = config.getString("jail.world", "world");
        double x = config.getDouble("jail.x", 0);
        double y = config.getDouble("jail.y", 64);
        double z = config.getDouble("jail.z", 0);
        float yaw = (float) config.getDouble("jail.yaw", 0);
        float pitch = (float) config.getDouble("jail.pitch", 0);

        jailLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public int getWantedLevel(UUID playerUUID) {
        return wantedLevels.getOrDefault(playerUUID, 0);
    }

    public void setWantedLevel(UUID playerUUID, int level) {
        // Ensure level is between 0 and 5
        level = Math.max(0, Math.min(5, level));

        wantedLevels.put(playerUUID, level);
        savePlayerData(playerUUID);

        // Notify player if online
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            if (level > 0) {
                player.sendMessage(ChatColor.RED + "Your wanted level is now: " + getWantedStars(level));
            } else {
                player.sendMessage(ChatColor.GREEN + "You are no longer wanted by the police.");
            }
        }
    }

    public void increaseWantedLevel(UUID playerUUID) {
        int currentLevel = getWantedLevel(playerUUID);
        if (currentLevel < 5) {
            setWantedLevel(playerUUID, currentLevel + 1);
        }
    }

    public void decreaseWantedLevel(UUID playerUUID) {
        int currentLevel = getWantedLevel(playerUUID);
        if (currentLevel > 0) {
            setWantedLevel(playerUUID, currentLevel - 1);
        }
    }

    public boolean isHandcuffed(UUID playerUUID) {
        return handcuffed.getOrDefault(playerUUID, false);
    }

    public void setHandcuffed(UUID playerUUID, boolean cuffed) {
        handcuffed.put(playerUUID, cuffed);
        savePlayerData(playerUUID);

        // Notify player if online
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            if (cuffed) {
                player.sendMessage(ChatColor.RED + "You have been handcuffed by the police!");
            } else {
                player.sendMessage(ChatColor.GREEN + "You have been uncuffed.");
            }
        }
    }

    public boolean isJailed(UUID playerUUID) {
        return jailTimes.containsKey(playerUUID) && jailTimes.get(playerUUID) > System.currentTimeMillis();
    }

    public long getRemainingJailTime(UUID playerUUID) {
        if (!isJailed(playerUUID)) {
            return 0;
        }

        return (jailTimes.get(playerUUID) - System.currentTimeMillis()) / 1000; // Convert to seconds
    }

    public void jailPlayer(UUID playerUUID, int minutes) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }

        // Calculate release time
        long releaseTime = System.currentTimeMillis() + (minutes * 60 * 1000);
        jailTimes.put(playerUUID, releaseTime);

        // Teleport to jail
        player.teleport(jailLocation);

        // Show message
        player.sendMessage(ChatColor.RED + "You have been jailed for " + minutes + " minutes!");

        // Cancel previous task if exists
        if (jailTasks.containsKey(playerUUID)) {
            jailTasks.get(playerUUID).cancel();
        }

        // Schedule release
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            releaseFromJail(playerUUID);
        }, minutes * 1200L); // 20 ticks per second, 60 seconds per minute

        jailTasks.put(playerUUID, task);
        savePlayerData(playerUUID);
    }

    public void releaseFromJail(UUID playerUUID) {
        jailTimes.remove(playerUUID);

        if (jailTasks.containsKey(playerUUID)) {
            jailTasks.get(playerUUID).cancel();
            jailTasks.remove(playerUUID);
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage(ChatColor.GREEN + "You have been released from jail!");

            // Teleport to spawn
            player.teleport(player.getWorld().getSpawnLocation());
        }

        savePlayerData(playerUUID);
    }

    public Location getJailLocation() {
        return jailLocation;
    }

    private String getWantedStars(int level) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < level) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    private void loadData() {
        if (dataFile.exists()) {
            for (String uuidString : dataConfig.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);

                // Load wanted level
                int wantedLevel = dataConfig.getInt(uuidString + ".wantedLevel", 0);
                wantedLevels.put(uuid, wantedLevel);

                // Load handcuffed status
                boolean isCuffed = dataConfig.getBoolean(uuidString + ".handcuffed", false);
                handcuffed.put(uuid, isCuffed);

                // Load jail time
                long jailTime = dataConfig.getLong(uuidString + ".jailTime", 0);
                if (jailTime > System.currentTimeMillis()) {
                    jailTimes.put(uuid, jailTime);

                    // Schedule release
                    long delayTicks = (jailTime - System.currentTimeMillis()) / 50; // Convert ms to ticks
                    BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        releaseFromJail(uuid);
                    }, delayTicks);

                    jailTasks.put(uuid, task);
                }
            }
        }
    }

    public void saveAllData() {
        // Save wanted levels
        for (Map.Entry<UUID, Integer> entry : wantedLevels.entrySet()) {
            String uuidString = entry.getKey().toString();
            int level = entry.getValue();

            dataConfig.set(uuidString + ".wantedLevel", level);
        }

        // Save handcuffed status
        for (Map.Entry<UUID, Boolean> entry : handcuffed.entrySet()) {
            String uuidString = entry.getKey().toString();
            boolean isCuffed = entry.getValue();

            dataConfig.set(uuidString + ".handcuffed", isCuffed);
        }

        // Save jail times
        for (Map.Entry<UUID, Long> entry : jailTimes.entrySet()) {
            String uuidString = entry.getKey().toString();
            long time = entry.getValue();

            dataConfig.set(uuidString + ".jailTime", time);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save police data: " + e.getMessage());
        }
    }

    public void savePlayerData(UUID playerUUID) {
        String uuidString = playerUUID.toString();

        // Save wanted level
        if (wantedLevels.containsKey(playerUUID)) {
            dataConfig.set(uuidString + ".wantedLevel", wantedLevels.get(playerUUID));
        }

        // Save handcuffed status
        if (handcuffed.containsKey(playerUUID)) {
            dataConfig.set(uuidString + ".handcuffed", handcuffed.get(playerUUID));
        }

        // Save jail time
        if (jailTimes.containsKey(playerUUID)) {
            dataConfig.set(uuidString + ".jailTime", jailTimes.get(playerUUID));
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save police data for player " + playerUUID + ": " + e.getMessage());
        }
    }
}