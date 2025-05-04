package me.ash.realitySMP.managers;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.Apartment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HousingManager {
    private final RealitySMP plugin;
    private final Map<String, Apartment> apartments;
    private final Map<UUID, String> playerHomes;
    private final Map<UUID, String> playerRentals;
    private final Map<UUID, String> playerOwnership;
    private final File housingDataFile;
    private final FileConfiguration housingData;

    public HousingManager(RealitySMP plugin) {
        this.plugin = plugin;
        this.apartments = new HashMap<>();
        this.playerHomes = new HashMap<>();
        this.playerRentals = new HashMap<>();
        this.playerOwnership = new HashMap<>();
        this.housingDataFile = new File(plugin.getDataFolder(), "housing.yml");
        this.housingData = YamlConfiguration.loadConfiguration(housingDataFile);

        loadApartments();
        loadPlayerHomes();
    }

    /**
     * Load apartments from config
     */
    private void loadApartments() {
        ConfigurationSection apartmentsSection = housingData.getConfigurationSection("apartments");
        if (apartmentsSection == null) return;

        for (String id : apartmentsSection.getKeys(false)) {
            ConfigurationSection apartmentSection = apartmentsSection.getConfigurationSection(id);
            if (apartmentSection == null) continue;

            String name = apartmentSection.getString("name", "Unnamed Apartment");
            String location = apartmentSection.getString("location", "0,0,0,world");
            double price = apartmentSection.getDouble("price", 1000);
            double rent = apartmentSection.getDouble("rent", 100);

            Apartment apartment = new Apartment(id, name, location, price, rent);
            apartments.put(id, apartment);
        }
    }

    /**
     * Load player home data
     */
    private void loadPlayerHomes() {
        ConfigurationSection homesSection = housingData.getConfigurationSection("player_homes");
        if (homesSection == null) return;

        for (String uuidStr : homesSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String apartmentId = homesSection.getString(uuidStr);
                playerHomes.put(uuid, apartmentId);

                // Load rental/ownership information
                boolean isRenting = housingData.getBoolean("player_rentals." + uuidStr, true);
                if (isRenting) {
                    playerRentals.put(uuid, apartmentId);
                } else {
                    playerOwnership.put(uuid, apartmentId);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in housing data: " + uuidStr);
            }
        }
    }

    /**
     * Save all housing data to file
     */
    public void saveHousingData() {
        // Save apartments
        ConfigurationSection apartmentsSection = housingData.createSection("apartments");
        for (Map.Entry<String, Apartment> entry : apartments.entrySet()) {
            String id = entry.getKey();
            Apartment apartment = entry.getValue();

            ConfigurationSection apartmentSection = apartmentsSection.createSection(id);
            apartmentSection.set("name", apartment.getName());
            apartmentSection.set("location", apartment.getLocation());
            apartmentSection.set("price", apartment.getPrice());
            apartmentSection.set("rent", apartment.getRent());
        }

        // Save player homes
        ConfigurationSection homesSection = housingData.createSection("player_homes");
        for (Map.Entry<UUID, String> entry : playerHomes.entrySet()) {
            UUID uuid = entry.getKey();
            homesSection.set(uuid.toString(), entry.getValue());

            // Save rental/ownership information
            boolean isRenting = playerRentals.containsKey(uuid);
            housingData.set("player_rentals." + uuid.toString(), isRenting);
        }

        try {
            housingData.save(housingDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save housing data: " + e.getMessage());
        }
    }

    /**
     * Get an apartment by ID
     *
     * @param id The apartment ID
     * @return The apartment, or null if not found
     */
    public Apartment getApartment(String id) {
        return apartments.get(id);
    }

    /**
     * Get all apartment IDs
     *
     * @return Set of apartment IDs
     */
    public Set<String> getApartmentIds() {
        return apartments.keySet();
    }

    /**
     * Check if an apartment is available (not owned by any player)
     *
     * @param id The apartment ID
     * @return true if the apartment exists and is available
     */
    public boolean isApartmentAvailable(String id) {
        if (!apartments.containsKey(id)) {
            return false;
        }

        return !playerHomes.containsValue(id);
    }

    /**
     * Check if a player has a home
     *
     * @param uuid The player's UUID
     * @return true if the player owns or rents a home
     */
    public boolean playerHasHome(UUID uuid) {
        return playerHomes.containsKey(uuid);
    }

    /**
     * Get a player's home apartment ID
     *
     * @param uuid The player's UUID
     * @return The apartment ID, or null if the player doesn't have a home
     */
    public String getPlayerHomeId(UUID uuid) {
        return playerHomes.get(uuid);
    }

    /**
     * Get a player's home apartment
     *
     * @param uuid The player's UUID
     * @return The apartment, or null if the player doesn't have a home
     */
    public Apartment getPlayerHome(UUID uuid) {
        String id = getPlayerHomeId(uuid);
        if (id == null) {
            return null;
        }

        return getApartment(id);
    }

    /**
     * Assign an apartment to a player
     *
     * @param uuid The player's UUID
     * @param apartmentId The apartment ID
     */
    public void assignHome(UUID uuid, String apartmentId) {
        playerHomes.put(uuid, apartmentId);
        saveHousingData();
    }

    /**
     * Remove a player's home
     *
     * @param uuid The player's UUID
     */
    public void removeHome(UUID uuid) {
        playerHomes.remove(uuid);
        playerRentals.remove(uuid);
        playerOwnership.remove(uuid);
        saveHousingData();
    }

    /**
     * Check if a player is renting (not owning) their apartment
     *
     * @param uuid The player's UUID
     * @return true if the player is renting an apartment
     */
    public boolean isPlayerRenting(UUID uuid) {
        return playerRentals.containsKey(uuid);
    }

    /**
     * Remove a player's home (same functionality as removeHome)
     *
     * @param uuid The player's UUID
     */
    public void removePlayerHome(UUID uuid) {
        // This is just an alias for the existing removeHome method for better readability
        removeHome(uuid);
    }

    /**
     * Assign an apartment to a player with ownership/rental information
     *
     * @param uuid The player's UUID
     * @param apartmentId The apartment ID
     * @param isRenting Whether the player is renting (true) or owns (false) the apartment
     */
    public void assignHomeToPlayer(UUID uuid, String apartmentId, boolean isRenting) {
        playerHomes.put(uuid, apartmentId);

        // Store whether the player is renting or owns the apartment
        if (isRenting) {
            playerRentals.put(uuid, apartmentId);
            playerOwnership.remove(uuid);
        } else {
            playerOwnership.put(uuid, apartmentId);
            playerRentals.remove(uuid);
        }

        saveHousingData();
    }

    /**
     * Check if a player owns (not rents) the specified apartment
     *
     * @param uuid The player's UUID
     * @param apartmentId The apartment ID
     * @return true if the player owns this apartment
     */
    public boolean playerOwnsHome(UUID uuid, String apartmentId) {
        // Player must have a home, the home must be the specified apartment,
        // and the player must not be renting
        return playerHasHome(uuid) &&
                getPlayerHomeId(uuid).equals(apartmentId) &&
                !isPlayerRenting(uuid);
    }
}