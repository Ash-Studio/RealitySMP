package me.ash.realitySMP.managers;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.PhoneApplication;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PhoneManager {
    private final RealitySMP plugin;
    private final Map<UUID, Boolean> playerPhones = new HashMap<>();
    private final Map<UUID, List<PhoneApplication>> playerApps = new HashMap<>();
    private final File phoneDataFile;
    private FileConfiguration phoneData;

    public PhoneManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.phoneDataFile = new File(plugin.getDataFolder(), "phone_data.yml");
        loadPhoneData();
    }

    private void loadPhoneData() {
        if (!phoneDataFile.exists()) {
            try {
                phoneDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create phone_data.yml: " + e.getMessage());
            }
        }

        phoneData = YamlConfiguration.loadConfiguration(phoneDataFile);

        // Load player phones
        ConfigurationSection phonesSection = phoneData.getConfigurationSection("phones");
        if (phonesSection != null) {
            for (String uuid : phonesSection.getKeys(false)) {
                UUID playerUuid = UUID.fromString(uuid);
                boolean hasPhone = phonesSection.getBoolean(uuid);
                playerPhones.put(playerUuid, hasPhone);
            }
        }

        // Load player apps
        ConfigurationSection appsSection = phoneData.getConfigurationSection("apps");
        if (appsSection != null) {
            for (String uuid : appsSection.getKeys(false)) {
                UUID playerUuid = UUID.fromString(uuid);
                List<String> appIds = appsSection.getStringList(uuid);
                List<PhoneApplication> apps = new ArrayList<>();

                for (String appId : appIds) {
                    PhoneApplication app = getAppById(appId);
                    if (app != null) {
                        apps.add(app);
                    }
                }

                playerApps.put(playerUuid, apps);
            }
        }
    }

    public void savePhoneData() {
        // Save player phones
        ConfigurationSection phonesSection = phoneData.createSection("phones");
        for (Map.Entry<UUID, Boolean> entry : playerPhones.entrySet()) {
            phonesSection.set(entry.getKey().toString(), entry.getValue());
        }

        // Save player apps
        ConfigurationSection appsSection = phoneData.createSection("apps");
        for (Map.Entry<UUID, List<PhoneApplication>> entry : playerApps.entrySet()) {
            List<String> appIds = new ArrayList<>();
            for (PhoneApplication app : entry.getValue()) {
                appIds.add(app.getId());
            }
            appsSection.set(entry.getKey().toString(), appIds);
        }

        try {
            phoneData.save(phoneDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save phone_data.yml: " + e.getMessage());
        }
    }

    public boolean hasPhone(UUID playerId) {
        return playerPhones.getOrDefault(playerId, false);
    }

    public void givePhone(UUID playerId) {
        playerPhones.put(playerId, true);

        // Add default apps
        if (!playerApps.containsKey(playerId)) {
            List<PhoneApplication> defaultApps = getDefaultApps();
            playerApps.put(playerId, defaultApps);
        }

        savePhoneData();
    }

    public void removePhone(UUID playerId) {
        playerPhones.put(playerId, false);
        savePhoneData();
    }

    public List<PhoneApplication> getPlayerApps(UUID playerId) {
        return playerApps.getOrDefault(playerId, new ArrayList<>());
    }

    public void addAppToPlayer(UUID playerId, String appId) {
        PhoneApplication app = getAppById(appId);
        if (app == null) {
            return;
        }

        List<PhoneApplication> apps = playerApps.getOrDefault(playerId, new ArrayList<>());

        // Check if player already has this app
        for (PhoneApplication playerApp : apps) {
            if (playerApp.getId().equals(appId)) {
                return;
            }
        }

        apps.add(app);
        playerApps.put(playerId, apps);
        savePhoneData();
    }

    public void removeAppFromPlayer(UUID playerId, String appId) {
        List<PhoneApplication> apps = playerApps.getOrDefault(playerId, new ArrayList<>());
        apps.removeIf(app -> app.getId().equals(appId));
        playerApps.put(playerId, apps);
        savePhoneData();
    }

    private List<PhoneApplication> getDefaultApps() {
        List<PhoneApplication> defaultApps = new ArrayList<>();
        defaultApps.add(new PhoneApplication("messages", "Messages", "Communicate with other players"));
        defaultApps.add(new PhoneApplication("banking", "Banking", "Access your bank account"));
        defaultApps.add(new PhoneApplication("map", "Map", "View a map of the area"));
        return defaultApps;
    }

    private PhoneApplication getAppById(String appId) {
        switch (appId) {
            case "messages":
                return new PhoneApplication("messages", "Messages", "Communicate with other players");
            case "banking":
                return new PhoneApplication("banking", "Banking", "Access your bank account");
            case "map":
                return new PhoneApplication("map", "Map", "View a map of the area");
            case "jobs":
                return new PhoneApplication("jobs", "Jobs", "Browse and apply for jobs");
            case "housing":
                return new PhoneApplication("housing", "Housing", "Browse and rent apartments");
            case "store":
                return new PhoneApplication("store", "App Store", "Download new applications");
            default:
                return null;
        }
    }

    public List<PhoneApplication> getAvailableApps() {
        List<PhoneApplication> availableApps = new ArrayList<>();
        availableApps.add(new PhoneApplication("messages", "Messages", "Communicate with other players"));
        availableApps.add(new PhoneApplication("banking", "Banking", "Access your bank account"));
        availableApps.add(new PhoneApplication("map", "Map", "View a map of the area"));
        availableApps.add(new PhoneApplication("jobs", "Jobs", "Browse and apply for jobs"));
        availableApps.add(new PhoneApplication("housing", "Housing", "Browse and rent apartments"));
        availableApps.add(new PhoneApplication("store", "App Store", "Download new applications"));
        return availableApps;
    }
}