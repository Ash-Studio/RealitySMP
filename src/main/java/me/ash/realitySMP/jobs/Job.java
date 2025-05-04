package me.ash.realitySMP.jobs;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Job {
    protected final RealitySMP plugin;
    protected final String name;
    protected final double baseSalary;
    protected final Map<UUID, Integer> jobLevels;
    protected final Map<UUID, Integer> jobExperience;

    public Job(RealitySMP plugin, String name, double baseSalary) {
        this.plugin = plugin;
        this.name = name;
        this.baseSalary = baseSalary;
        this.jobLevels = new HashMap<>();
        this.jobExperience = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public int getJobLevel(UUID playerId) {
        return jobLevels.getOrDefault(playerId, 1);
    }

    public void setJobLevel(UUID playerId, int level) {
        jobLevels.put(playerId, level);
    }

    public int getJobExperience(UUID playerId) {
        return jobExperience.getOrDefault(playerId, 0);
    }

    public void setJobExperience(UUID playerId, int experience) {
        jobExperience.put(playerId, experience);
    }

    public void addJobExperience(UUID playerId, int amount) {
        int currentExp = getJobExperience(playerId);
        int currentLevel = getJobLevel(playerId);
        int newExp = currentExp + amount;

        // Simple leveling system: Level up every 100 XP
        if (newExp >= 100 * currentLevel) {
            setJobLevel(playerId, currentLevel + 1);
            setJobExperience(playerId, newExp - (100 * currentLevel));
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§aYou've leveled up in your " + name + " job! You are now level " + (currentLevel + 1));
            }
        } else {
            setJobExperience(playerId, newExp);
        }
    }

    public double getSalary(UUID playerId) {
        int level = getJobLevel(playerId);
        // 10% increase per level
        return baseSalary * (1 + (0.1 * (level - 1)));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Integer> serializedLevels = new HashMap<>();
        Map<String, Integer> serializedExperience = new HashMap<>();

        for (Map.Entry<UUID, Integer> entry : jobLevels.entrySet()) {
            serializedLevels.put(entry.getKey().toString(), entry.getValue());
        }

        for (Map.Entry<UUID, Integer> entry : jobExperience.entrySet()) {
            serializedExperience.put(entry.getKey().toString(), entry.getValue());
        }

        data.put("levels", serializedLevels);
        data.put("experience", serializedExperience);
        return data;
    }

    @SuppressWarnings("unchecked")
    public void deserialize(Map<String, Object> data) {
        jobLevels.clear();
        jobExperience.clear();

        Map<String, Integer> serializedLevels = (Map<String, Integer>) data.get("levels");
        Map<String, Integer> serializedExperience = (Map<String, Integer>) data.get("experience");

        if (serializedLevels != null) {
            for (Map.Entry<String, Integer> entry : serializedLevels.entrySet()) {
                jobLevels.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
        }

        if (serializedExperience != null) {
            for (Map.Entry<String, Integer> entry : serializedExperience.entrySet()) {
                jobExperience.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
        }
    }

    // Abstract methods that each job must implement
    public abstract void onJobJoin(Player player);
    public abstract void onJobLeave(Player player);
    public abstract boolean onJobAction(Player player, String action);
}