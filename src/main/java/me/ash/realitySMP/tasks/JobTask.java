package me.ash.realitySMP.tasks;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.BankAccount;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JobTask {
    private final RealitySMP plugin;
    private final Map<UUID, String> playerJobs = new HashMap<>();
    private final Map<String, Double> jobSalaries = new HashMap<>();
    private File jobsFile;
    private FileConfiguration jobsConfig;
    private BukkitRunnable paydayTask;

    public JobTask(RealitySMP plugin) {
        this.plugin = plugin;
        this.jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        loadJobsConfig();
        setupSalaries();
        startPaydayTask();
    }

    private void loadJobsConfig() {
        if (!jobsFile.exists()) {
            try {
                jobsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create jobs.yml file: " + e.getMessage());
            }
        }

        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);

        // Load player jobs
        if (jobsConfig.contains("players")) {
            for (String uuidString : jobsConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String job = jobsConfig.getString("players." + uuidString);
                playerJobs.put(uuid, job);
            }
        }
    }

    private void setupSalaries() {
        // Default job salaries - can be overridden by config
        jobSalaries.put("miner", 150.0);
        jobSalaries.put("farmer", 120.0);
        jobSalaries.put("lumberjack", 110.0);
        jobSalaries.put("hunter", 140.0);
        jobSalaries.put("fisherman", 130.0);
        jobSalaries.put("chef", 160.0);
        jobSalaries.put("builder", 180.0);
        jobSalaries.put("merchant", 200.0);

        // Load from config if exists
        if (jobsConfig.contains("salaries")) {
            for (String job : jobsConfig.getConfigurationSection("salaries").getKeys(false)) {
                double salary = jobsConfig.getDouble("salaries." + job);
                jobSalaries.put(job.toLowerCase(), salary);
            }
        } else {
            // Save defaults to config
            for (Map.Entry<String, Double> entry : jobSalaries.entrySet()) {
                jobsConfig.set("salaries." + entry.getKey(), entry.getValue());
            }
            saveJobsConfig();
        }
    }

    public void setPlayerJob(UUID playerUUID, String job) {
        job = job.toLowerCase();
        if (!jobSalaries.containsKey(job)) {
            throw new IllegalArgumentException("Job '" + job + "' does not exist!");
        }

        playerJobs.put(playerUUID, job);
        jobsConfig.set("players." + playerUUID.toString(), job);
        saveJobsConfig();

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            player.sendMessage("§a§lYour job has been set to: §e" + job + "§a!");
            player.sendMessage("§a§lYou will earn §e$" + jobSalaries.get(job) + "§a per payday.");
        }
    }

    public String getPlayerJob(UUID playerUUID) {
        return playerJobs.getOrDefault(playerUUID, null);
    }

    public double getJobSalary(String job) {
        job = job.toLowerCase();
        return jobSalaries.getOrDefault(job, 0.0);
    }

    public void saveJobsConfig() {
        try {
            jobsConfig.save(jobsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save jobs.yml file: " + e.getMessage());
        }
    }

    private void startPaydayTask() {
        // Run payday every 2 hours (2 * 60 * 60 * 20 = 144000 ticks)
        paydayTask = new BukkitRunnable() {
            @Override
            public void run() {
                payAllSalaries();
            }
        };
        paydayTask.runTaskTimer(plugin, 144000, 144000);
    }

    public void payAllSalaries() {
        Bukkit.broadcastMessage("§6§l[PAYDAY] §eSalaries have been deposited to all accounts!");

        for (Map.Entry<UUID, String> entry : playerJobs.entrySet()) {
            UUID playerUUID = entry.getKey();
            String job = entry.getValue();
            double salary = jobSalaries.getOrDefault(job, 0.0);

            if (salary > 0) {
                BankAccount account = BankAccount.getAccount(playerUUID);
                account.deposit(salary, "Salary for " + job);

                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§a§lYou received §e$" + salary + "§a for your job as a §e" + job + "§a!");
                }
            }
        }

        // Save all accounts after payday
        BankAccount.saveAccounts();
    }

    public void shutdown() {
        if (paydayTask != null) {
            paydayTask.cancel();
        }
        saveJobsConfig();
    }

    public Map<String, Double> getAvailableJobs() {
        return new HashMap<>(jobSalaries);
    }
}