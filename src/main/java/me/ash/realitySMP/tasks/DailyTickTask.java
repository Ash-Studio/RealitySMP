package me.ash.realitySMP.tasks;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.scheduler.BukkitRunnable;

public class DailyTickTask extends BukkitRunnable {

    private final RealitySMP plugin;
    private int tickCounter = 0;

    // Config values for tick intervals
    private final int moodTickInterval;
    private final int hygieneTickInterval;
    private final int energyTickInterval;

    public DailyTickTask(RealitySMP plugin) {
        this.plugin = plugin;

        // Load tick intervals from config (in minutes)
        moodTickInterval = plugin.getConfig().getInt("tick-intervals.mood", 5);
        hygieneTickInterval = plugin.getConfig().getInt("tick-intervals.hygiene", 10);
        energyTickInterval = plugin.getConfig().getInt("tick-intervals.energy", 8);
    }

    @Override
    public void run() {
        tickCounter++;

        // Process mood reduction (every X minutes)
        if (tickCounter % moodTickInterval == 0) {
            plugin.getMoodManager().decreaseMoodTick();
        }

        // Process hygiene reduction (every X minutes)
        if (tickCounter % hygieneTickInterval == 0) {
            plugin.getHygieneManager().decreaseHygieneTick();
        }

        // Process energy reduction (every X minutes)
        if (tickCounter % energyTickInterval == 0) {
            plugin.getEnergyManager().decreaseEnergyTick();
        }
    }
}