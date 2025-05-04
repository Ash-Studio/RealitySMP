package me.ash.realitySMP;

import me.ash.realitySMP.commands.*;
import me.ash.realitySMP.listeners.*;
import me.ash.realitySMP.managers.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class RealitySMP extends JavaPlugin {

    // Manager instances
    private JobManager jobManager;
    private BankManager bankManager;
    private HousingManager housingManager;
    private EnergyManager energyManager;
    private PoliceManager policeManager;
    private PhoneManager phoneManager;

    // Config files
    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable() {
        // Create config files if they don't exist
        saveDefaultConfig();
        createCustomConfig();

        // Initialize managers
        jobManager = new JobManager(this);
        bankManager = new BankManager(this);
        housingManager = new HousingManager(this);
        energyManager = new EnergyManager(this);
        policeManager = new PoliceManager(this);
        phoneManager = new PhoneManager(this);

        // Register commands and listeners
        registerCommands();
        registerListeners();

        getLogger().info("RealitySMP has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data when the plugin disables
        savePendingData();

        getLogger().info("RealitySMP has been disabled!");
    }

    private void registerCommands() {
        // Register all commands
        getCommand("atm").setExecutor(new ATMCommand(this));
        getCommand("buyhome").setExecutor(new BuyHomeCommand(this));
        getCommand("cuff").setExecutor(new CuffCommand(this));
        getCommand("id").setExecutor(new IdCommand(this));
        getCommand("jail").setExecutor(new JailCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("phone").setExecutor(new PhoneCommand(this));
        // Add other commands here
    }

    private void registerListeners() {
        // Register all event listeners
        getServer().getPluginManager().registerEvents(new JobGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        // Add other listeners here
    }

    private void savePendingData() {
        try {
            // Save data from all managers
            saveCustomConfig();
            // Additional saving logic for managers
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to save plugin data!", e);
        }
    }

    // Custom config methods
    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "custom.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("custom.yml", false);
        }

        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public void saveCustomConfig() {
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save custom config to " + customConfigFile, e);
        }
    }

    // Getter methods for managers
    public JobManager getJobManager() {
        return jobManager;
    }

    public BankManager getBankManager() {
        return bankManager;
    }

    public HousingManager getHousingManager() {
        return housingManager;
    }

    public EnergyManager getEnergyManager() {
        return energyManager;
    }

    public PoliceManager getPoliceManager() {
        return policeManager;
    }

    public PhoneManager getPhoneManager() {
        return phoneManager;
    }
}