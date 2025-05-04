package me.ash.realitySMP.managers;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankManager {
    private final RealitySMP plugin;
    private final Map<UUID, Double> bankAccounts;
    private final File bankDataFile;
    private final FileConfiguration bankData;

    public BankManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.bankAccounts = new HashMap<>();
        this.bankDataFile = new File(plugin.getDataFolder(), "bank-data.yml");
        this.bankData = YamlConfiguration.loadConfiguration(bankDataFile);

        loadBankData();
    }

    /**
     * Load all bank account data from file
     */
    private void loadBankData() {
        if (bankData.contains("accounts")) {
            for (String uuidString : bankData.getConfigurationSection("accounts").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    double balance = bankData.getDouble("accounts." + uuidString);
                    bankAccounts.put(uuid, balance);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in bank data: " + uuidString);
                }
            }
        }
    }

    /**
     * Save all bank account data to file
     */
    public void saveBankData() {
        // Clear existing data
        bankData.set("accounts", null);

        // Save all accounts
        for (Map.Entry<UUID, Double> entry : bankAccounts.entrySet()) {
            String path = "accounts." + entry.getKey().toString();
            bankData.set(path, entry.getValue());
        }

        // Save to file
        try {
            bankData.save(bankDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save bank data: " + e.getMessage());
        }
    }

    /**
     * Get a player's bank account balance
     *
     * @param uuid The player's UUID
     * @return The player's balance
     */
    public double getBalance(UUID uuid) {
        return bankAccounts.getOrDefault(uuid, 0.0);
    }

    /**
     * Add money to a player's bank account
     *
     * @param uuid The player's UUID
     * @param amount The amount to add
     */
    public void addToBank(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);
        bankAccounts.put(uuid, currentBalance + amount);
    }

    /**
     * Remove money from a player's bank account
     *
     * @param uuid The player's UUID
     * @param amount The amount to withdraw
     * @return true if successful, false if insufficient funds
     */
    public boolean withdrawFromBank(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);

        if (currentBalance < amount) {
            return false;
        }

        bankAccounts.put(uuid, currentBalance - amount);
        return true;
    }

    /**
     * Set a player's bank account balance
     *
     * @param uuid The player's UUID
     * @param amount The new balance
     */
    public void setBalance(UUID uuid, double amount) {
        bankAccounts.put(uuid, amount);
    }

    /**
     * Check if a player has at least a certain amount in their account
     *
     * @param uuid The player's UUID
     * @param amount The amount to check for
     * @return true if the player has enough money, false otherwise
     */
    public boolean hasEnough(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }
}