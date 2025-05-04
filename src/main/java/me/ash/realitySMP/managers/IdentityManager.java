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

public class IdentityManager {
    private final RealitySMP plugin;
    private final Map<UUID, PlayerIdentity> playerIdentities = new HashMap<>();
    private final File identityFile;
    private FileConfiguration identityConfig;

    public IdentityManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.identityFile = new File(plugin.getDataFolder(), "identities.yml");
        loadIdentities();
    }

    public void loadIdentities() {
        if (!identityFile.exists()) {
            try {
                identityFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create identities.yml file: " + e.getMessage());
            }
        }

        identityConfig = YamlConfiguration.loadConfiguration(identityFile);

        // Load identities for all players
        if (identityConfig.contains("identities")) {
            for (String uuidString : identityConfig.getConfigurationSection("identities").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String name = identityConfig.getString("identities." + uuidString + ".name");
                String address = identityConfig.getString("identities." + uuidString + ".address");
                int age = identityConfig.getInt("identities." + uuidString + ".age");
                String backstory = identityConfig.getString("identities." + uuidString + ".backstory", "");

                PlayerIdentity identity = new PlayerIdentity(name, address, age, backstory);
                playerIdentities.put(uuid, identity);
            }
        }
    }

    public void saveIdentities() {
        // Save all identities
        for (Map.Entry<UUID, PlayerIdentity> entry : playerIdentities.entrySet()) {
            String uuidString = entry.getKey().toString();
            PlayerIdentity identity = entry.getValue();

            identityConfig.set("identities." + uuidString + ".name", identity.getName());
            identityConfig.set("identities." + uuidString + ".address", identity.getAddress());
            identityConfig.set("identities." + uuidString + ".age", identity.getAge());
            identityConfig.set("identities." + uuidString + ".backstory", identity.getBackstory());
        }

        try {
            identityConfig.save(identityFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save identities.yml file: " + e.getMessage());
        }
    }

    public PlayerIdentity getIdentity(UUID playerUUID) {
        return playerIdentities.getOrDefault(playerUUID, createDefaultIdentity(playerUUID));
    }

    private PlayerIdentity createDefaultIdentity(UUID playerUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        String name = player != null ? player.getName() : "Unknown";
        PlayerIdentity identity = new PlayerIdentity(name, "Homeless", 25, "A newcomer to Reality SMP.");
        setIdentity(playerUUID, identity);
        return identity;
    }

    public void setIdentity(UUID playerUUID, PlayerIdentity identity) {
        playerIdentities.put(playerUUID, identity);
    }

    public void updateIdentityName(UUID playerUUID, String name) {
        PlayerIdentity identity = getIdentity(playerUUID);
        identity.setName(name);
        setIdentity(playerUUID, identity);
    }

    public void updateIdentityAddress(UUID playerUUID, String address) {
        PlayerIdentity identity = getIdentity(playerUUID);
        identity.setAddress(address);
        setIdentity(playerUUID, identity);
    }

    public void updateIdentityAge(UUID playerUUID, int age) {
        PlayerIdentity identity = getIdentity(playerUUID);
        identity.setAge(age);
        setIdentity(playerUUID, identity);
    }

    public void updateIdentityBackstory(UUID playerUUID, String backstory) {
        PlayerIdentity identity = getIdentity(playerUUID);
        identity.setBackstory(backstory);
        setIdentity(playerUUID, identity);
    }

    public static class PlayerIdentity {
        private String name;
        private String address;
        private int age;
        private String backstory;

        public PlayerIdentity(String name, String address, int age, String backstory) {
            this.name = name;
            this.address = address;
            this.age = age;
            this.backstory = backstory;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getBackstory() {
            return backstory;
        }

        public void setBackstory(String backstory) {
            this.backstory = backstory;
        }
    }
}