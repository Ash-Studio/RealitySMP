package me.ash.realitySMP.model;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankAccount {
    private final UUID ownerUUID;
    private double balance;
    private final Map<String, Double> transactions;
    private static final Map<UUID, BankAccount> accounts = new HashMap<>();
    private static File accountsFile;
    private static FileConfiguration accountsConfig;

    public BankAccount(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.balance = 0.0;
        this.transactions = new HashMap<>();
        accounts.put(ownerUUID, this);
    }

    public static void initialize(RealitySMP plugin) {
        accountsFile = new File(plugin.getDataFolder(), "accounts.yml");
        if (!accountsFile.exists()) {
            try {
                accountsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create accounts.yml file: " + e.getMessage());
            }
        }

        accountsConfig = YamlConfiguration.loadConfiguration(accountsFile);
        loadAccounts();
    }

    private static void loadAccounts() {
        if (accountsConfig.contains("accounts")) {
            for (String uuidString : accountsConfig.getConfigurationSection("accounts").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                double balance = accountsConfig.getDouble("accounts." + uuidString + ".balance");
                BankAccount account = new BankAccount(uuid);
                account.setBalance(balance);

                // Load transaction history if available
                if (accountsConfig.contains("accounts." + uuidString + ".transactions")) {
                    for (String txnId : accountsConfig.getConfigurationSection("accounts." + uuidString + ".transactions").getKeys(false)) {
                        double amount = accountsConfig.getDouble("accounts." + uuidString + ".transactions." + txnId);
                        account.transactions.put(txnId, amount);
                    }
                }
            }
        }
    }

    public static void saveAccounts() {
        for (Map.Entry<UUID, BankAccount> entry : accounts.entrySet()) {
            String uuidString = entry.getKey().toString();
            accountsConfig.set("accounts." + uuidString + ".balance", entry.getValue().getBalance());

            // Save transaction history
            for (Map.Entry<String, Double> txn : entry.getValue().transactions.entrySet()) {
                accountsConfig.set("accounts." + uuidString + ".transactions." + txn.getKey(), txn.getValue());
            }
        }

        try {
            accountsConfig.save(accountsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BankAccount getAccount(UUID playerUUID) {
        if (!accounts.containsKey(playerUUID)) {
            return new BankAccount(playerUUID);
        }
        return accounts.get(playerUUID);
    }

    public static BankAccount getAccount(Player player) {
        return getAccount(player.getUniqueId());
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean withdraw(double amount, String reason) {
        if (amount <= 0) {
            return false;
        }

        if (balance >= amount) {
            balance -= amount;
            recordTransaction("WITHDRAW-" + System.currentTimeMillis() + "-" + reason, -amount);
            return true;
        }
        return false;
    }

    public void deposit(double amount, String reason) {
        if (amount <= 0) {
            return;
        }

        balance += amount;
        recordTransaction("DEPOSIT-" + System.currentTimeMillis() + "-" + reason, amount);
    }

    public boolean transfer(BankAccount recipient, double amount, String reason) {
        if (amount <= 0) {
            return false;
        }

        if (balance >= amount) {
            balance -= amount;
            recipient.deposit(amount, "Transfer from " + ownerUUID.toString());
            recordTransaction("TRANSFER-" + System.currentTimeMillis() + "-TO-" + recipient.ownerUUID.toString() + "-" + reason, -amount);
            return true;
        }
        return false;
    }

    private void recordTransaction(String id, double amount) {
        transactions.put(id, amount);
    }

    public Map<String, Double> getTransactionHistory() {
        return new HashMap<>(transactions);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }
}