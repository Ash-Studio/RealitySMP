package me.ash.realitySMP.gui;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.jobs.Job;
import me.ash.realitySMP.jobs.JobManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JobGUI {
    private final RealitySMP plugin;

    public JobGUI(RealitySMP plugin) {
        this.plugin = plugin;
    }

    public void openJobSelectionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Job Selection");

        JobManager jobManager = plugin.getJobManager();
        Map<String, Job> availableJobs = jobManager.getAvailableJobs();
        Job currentJob = jobManager.getPlayerJob(player.getUniqueId());

        // Police Officer
        createJobItem(gui, 10, Material.IRON_SWORD, "§9Police Officer",
                "Protect citizens and arrest criminals",
                "$150 base salary",
                currentJob != null && currentJob.getName().equals("Police Officer"));

        // Miner
        createJobItem(gui, 11, Material.IRON_PICKAXE, "§6Miner",
                "Mine ores to earn money",
                "$100 base salary",
                currentJob != null && currentJob.getName().equals("Miner"));

        // Doctor
        createJobItem(gui, 12, Material.GOLDEN_APPLE, "§cDoctor",
                "Heal injured players",
                "$130 base salary",
                currentJob != null && currentJob.getName().equals("Doctor"));

        // Mechanic
        createJobItem(gui, 13, Material.ANVIL, "§7Mechanic",
                "Repair tools and equipment",
                "$120 base salary",
                currentJob != null && currentJob.getName().equals("Mechanic"));

        // Taxi Driver
        createJobItem(gui, 14, Material.MINECART, "§eWild Hunter",
                "Hunt wildlife for resources",
                "$110 base salary",
                currentJob != null && currentJob.getName().equals("Taxi Driver"));

        // Leave job button
        if (currentJob != null) {
            ItemStack leaveJob = new ItemStack(Material.BARRIER);
            ItemMeta meta = leaveJob.getItemMeta();
            meta.setDisplayName("§cLeave Current Job");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to leave your current job as a");
            lore.add("§7" + currentJob.getName());
            meta.setLore(lore);
            leaveJob.setItemMeta(meta);
            gui.setItem(22, leaveJob);
        }

        player.openInventory(gui);
    }

    private void createJobItem(Inventory inv, int slot, Material material, String name, String description, String salary, boolean isCurrentJob) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("§7" + description);
        lore.add("§7" + salary);

        if (isCurrentJob) {
            lore.add("");
            lore.add("§aThis is your current job");
        } else {
            lore.add("");
            lore.add("§eClick to join this job");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    public void openJobStatsGUI(Player player) {
        JobManager jobManager = plugin.getJobManager();
        Job job = jobManager.getPlayerJob(player.getUniqueId());

        if (job == null) {
            player.sendMessage("§cYou don't have a job! Use /job to join one.");
            return;
        }

        UUID playerId = player.getUniqueId();
        int level = job.getJobLevel(playerId);
        int exp = job.getJobExperience(playerId);
        int expNeeded = level * 100;
        double salary = job.getSalary(playerId);

        Inventory gui = Bukkit.createInventory(null, 27, "§8Job Statistics");

        // Job info
        ItemStack jobInfo = new ItemStack(Material.NAME_TAG);
        ItemMeta jobMeta = jobInfo.getItemMeta();
        jobMeta.setDisplayName("§e" + job.getName());
        List<String> jobLore = new ArrayList<>();
        jobLore.add("§7Your current job");
        jobInfo.setItemMeta(jobMeta);
        gui.setItem(4, jobInfo);

        // Level info
        ItemStack levelInfo = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta levelMeta = levelInfo.getItemMeta();
        levelMeta.setDisplayName("§aLevel " + level);
        List<String> levelLore = new ArrayList<>();
        levelLore.add("§7Experience: §a" + exp + "§7/§a" + expNeeded);
        levelLore.add("§7Progress: §a" + (int)((double)exp / expNeeded * 100) + "%");
        levelMeta.setLore(levelLore);
        levelInfo.setItemMeta(levelMeta);
        gui.setItem(11, levelInfo);

        // Salary info
        ItemStack salaryInfo = new ItemStack(Material.GOLD_INGOT);
        ItemMeta salaryMeta = salaryInfo.getItemMeta();
        salaryMeta.setDisplayName("§6Salary: $" + salary);
        List<String> salaryLore = new ArrayList<>();
        salaryLore.add("§7Base salary: §6$" + job.getBaseSalary());
        salaryLore.add("§7Level bonus: §6+" + (int)((salary - job.getBaseSalary()) * 100 / job.getBaseSalary()) + "%");
        salaryMeta.setLore(salaryLore);
        salaryInfo.setItemMeta(salaryMeta);
        gui.setItem(15, salaryInfo);

        player.openInventory(gui);
    }
}