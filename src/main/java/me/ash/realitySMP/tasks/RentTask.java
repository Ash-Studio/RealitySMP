package me.ash.realitySMP.tasks;

import me.ash.realitySMP.RealitySMP;
import me.ash.realitySMP.model.BankAccount;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RentTask {
    private final RealitySMP plugin;
    private File propertyFile;
    private FileConfiguration propertyConfig;
    private Map<String, PropertyInfo> properties = new HashMap<>();
    private BukkitRunnable rentCollectionTask;

    public RentTask(RealitySMP plugin) {
        this.plugin = plugin;
        this.propertyFile = new File(plugin.getDataFolder(), "properties.yml");
        loadPropertyConfig();
        startRentCollectionTask();
    }

    private void loadPropertyConfig() {
        if (!propertyFile.exists()) {
            try {
                propertyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create properties.yml file: " + e.getMessage());
            }
        }

        propertyConfig = YamlConfiguration.loadConfiguration(propertyFile);

        // Load properties
        if (propertyConfig.contains("properties")) {
            for (String propertyId : propertyConfig.getConfigurationSection("properties").getKeys(false)) {
                String name = propertyConfig.getString("properties." + propertyId + ".name");
                String world = propertyConfig.getString("properties." + propertyId + ".world");
                double x1 = propertyConfig.getDouble("properties." + propertyId + ".x1");
                double y1 = propertyConfig.getDouble("properties." + propertyId + ".y1");
                double z1 = propertyConfig.getDouble("properties." + propertyId + ".z1");
                double x2 = propertyConfig.getDouble("properties." + propertyId + ".x2");
                double y2 = propertyConfig.getDouble("properties." + propertyId + ".y2");
                double z2 = propertyConfig.getDouble("properties." + propertyId + ".z2");
                double rentAmount = propertyConfig.getDouble("properties." + propertyId + ".rent");
                String ownerUUIDString = propertyConfig.getString("properties." + propertyId + ".owner");
                UUID ownerUUID = ownerUUIDString != null ? UUID.fromString(ownerUUIDString) : null;

                List<String> tenantsStringList = propertyConfig.getStringList("properties." + propertyId + ".tenants");
                List<UUID> tenants = new ArrayList<>();
                for (String tenant : tenantsStringList) {
                    tenants.add(UUID.fromString(tenant));
                }

                PropertyInfo property = new PropertyInfo(propertyId, name, world, x1, y1, z1, x2, y2, z2, rentAmount, ownerUUID, tenants);
                properties.put(propertyId, property);
            }
        }
    }

    public void savePropertyConfig() {
        for (Map.Entry<String, PropertyInfo> entry : properties.entrySet()) {
            String propertyId = entry.getKey();
            PropertyInfo property = entry.getValue();

            propertyConfig.set("properties." + propertyId + ".name", property.getName());
            propertyConfig.set("properties." + propertyId + ".world", property.getWorld());
            propertyConfig.set("properties." + propertyId + ".x1", property.getX1());
            propertyConfig.set("properties." + propertyId + ".y1", property.getY1());
            propertyConfig.set("properties." + propertyId + ".z1", property.getZ1());
            propertyConfig.set("properties." + propertyId + ".x2", property.getX2());
            propertyConfig.set("properties." + propertyId + ".y2", property.getY2());
            propertyConfig.set("properties." + propertyId + ".z2", property.getZ2());
            propertyConfig.set("properties." + propertyId + ".rent", property.getRentAmount());

            if (property.getOwnerUUID() != null) {
                propertyConfig.set("properties." + propertyId + ".owner", property.getOwnerUUID().toString());
            } else {
                propertyConfig.set("properties." + propertyId + ".owner", null);
            }

            List<String> tenantsStringList = new ArrayList<>();
            for (UUID tenant : property.getTenants()) {
                tenantsStringList.add(tenant.toString());
            }
            propertyConfig.set("properties." + propertyId + ".tenants", tenantsStringList);
        }

        try {
            propertyConfig.save(propertyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save properties.yml file: " + e.getMessage());
        }
    }

    private void startRentCollectionTask() {
        // Collect rent every day (24 * 60 * 60 * 20 = 1,728,000 ticks)
        rentCollectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                collectAllRent();
            }
        };
        rentCollectionTask.runTaskTimer(plugin, 1728000, 1728000);
    }

    public void collectAllRent() {
        Bukkit.broadcastMessage("§6§l[RENT COLLECTION] §eRent is now being collected for all properties!");

        for (PropertyInfo property : properties.values()) {
            if (property.getOwnerUUID() != null && !property.getTenants().isEmpty()) {
                double rentPerTenant = property.getRentAmount() / property.getTenants().size();

                for (UUID tenantUUID : property.getTenants()) {
                    BankAccount tenantAccount = BankAccount.getAccount(tenantUUID);
                    boolean successful = tenantAccount.withdraw(rentPerTenant, "Rent for " + property.getName());

                    if (successful) {
                        // Transfer money to property owner
                        BankAccount ownerAccount = BankAccount.getAccount(property.getOwnerUUID());
                        ownerAccount.deposit(rentPerTenant, "Rent payment from tenant for " + property.getName());

                        // Notify tenant
                        Player tenant = Bukkit.getPlayer(tenantUUID);
                        if (tenant != null && tenant.isOnline()) {
                            tenant.sendMessage("§a§lYou paid §e$" + rentPerTenant + "§a in rent for §e" + property.getName() + "§a.");
                        }

                        // Notify owner
                        Player owner = Bukkit.getPlayer(property.getOwnerUUID());
                        if (owner != null && owner.isOnline()) {
                            owner.sendMessage("§a§lYou received §e$" + rentPerTenant + "§a in rent from a tenant for §e" + property.getName() + "§a.");
                        }
                    } else {
                        // Tenant couldn't pay rent
                        Player tenant = Bukkit.getPlayer(tenantUUID);
                        if (tenant != null && tenant.isOnline()) {
                            tenant.sendMessage("§c§lYou couldn't afford to pay §e$" + rentPerTenant + "§c for §e" + property.getName() + "§c!");
                            tenant.sendMessage("§c§lYou may be evicted if you continue to miss payments.");
                        }

                        // Notify owner
                        Player owner = Bukkit.getPlayer(property.getOwnerUUID());
                        if (owner != null && owner.isOnline()) {
                            owner.sendMessage("§c§lA tenant couldn't pay rent for §e" + property.getName() + "§c!");
                        }
                    }
                }
            }
        }

        // Save all accounts after rent collection
        BankAccount.saveAccounts();
    }

    public PropertyInfo createProperty(String name, String world, double x1, double y1, double z1,
                                       double x2, double y2, double z2, double rentAmount) {
        String propertyId = UUID.randomUUID().toString().substring(0, 8);
        PropertyInfo property = new PropertyInfo(propertyId, name, world, x1, y1, z1, x2, y2, z2, rentAmount, null, new ArrayList<>());
        properties.put(propertyId, property);
        savePropertyConfig();
        return property;
    }

    public PropertyInfo getProperty(String propertyId) {
        return properties.get(propertyId);
    }

    public List<PropertyInfo> getAllProperties() {
        return new ArrayList<>(properties.values());
    }

    public List<PropertyInfo> getAvailableProperties() {
        List<PropertyInfo> available = new ArrayList<>();
        for (PropertyInfo property : properties.values()) {
            if (property.getOwnerUUID() == null) {
                available.add(property);
            }
        }
        return available;
    }

    public List<PropertyInfo> getPlayerOwnedProperties(UUID playerUUID) {
        List<PropertyInfo> owned = new ArrayList<>();
        for (PropertyInfo property : properties.values()) {
            if (property.getOwnerUUID() != null && property.getOwnerUUID().equals(playerUUID)) {
                owned.add(property);
            }
        }
        return owned;
    }

    public List<PropertyInfo> getPlayerRentedProperties(UUID playerUUID) {
        List<PropertyInfo> rented = new ArrayList<>();
        for (PropertyInfo property : properties.values()) {
            if (property.getTenants().contains(playerUUID)) {
                rented.add(property);
            }
        }
        return rented;
    }

    public void setPropertyOwner(String propertyId, UUID ownerUUID) {
        PropertyInfo property = properties.get(propertyId);
        if (property != null) {
            property.setOwnerUUID(ownerUUID);
            savePropertyConfig();
        }
    }

    public void addTenant(String propertyId, UUID tenantUUID) {
        PropertyInfo property = properties.get(propertyId);
        if (property != null) {
            property.addTenant(tenantUUID);
            savePropertyConfig();
        }
    }

    public void removeTenant(String propertyId, UUID tenantUUID) {
        PropertyInfo property = properties.get(propertyId);
        if (property != null) {
            property.removeTenant(tenantUUID);
            savePropertyConfig();
        }
    }

    public void setRentAmount(String propertyId, double rentAmount) {
        PropertyInfo property = properties.get(propertyId);
        if (property != null) {
            property.setRentAmount(rentAmount);
            savePropertyConfig();
        }
    }

    public boolean isInProperty(Location location) {
        for (PropertyInfo property : properties.values()) {
            if (property.isInside(location)) {
                return true;
            }
        }
        return false;
    }

    public PropertyInfo getPropertyAt(Location location) {
        for (PropertyInfo property : properties.values()) {
            if (property.isInside(location)) {
                return property;
            }
        }
        return null;
    }

    public void shutdown() {
        if (rentCollectionTask != null) {
            rentCollectionTask.cancel();
        }
        savePropertyConfig();
    }

    public static class PropertyInfo {
        private final String id;
        private final String name;
        private final String world;
        private final double x1, y1, z1, x2, y2, z2;
        private double rentAmount;
        private UUID ownerUUID;
        private List<UUID> tenants;

        public PropertyInfo(String id, String name, String world,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double rentAmount, UUID ownerUUID, List<UUID> tenants) {
            this.id = id;
            this.name = name;
            this.world = world;
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
            this.rentAmount = rentAmount;
            this.ownerUUID = ownerUUID;
            this.tenants = tenants;
        }

        public boolean isInside(Location location) {
            if (!location.getWorld().getName().equals(world)) {
                return false;
            }

            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            return x >= x1 && x <= x2 &&
                    y >= y1 && y <= y2 &&
                    z >= z1 && z <= z2;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getWorld() {
            return world;
        }

        public double getX1() {
            return x1;
        }

        public double getY1() {
            return y1;
        }

        public double getZ1() {
            return z1;
        }

        public double getX2() {
            return x2;
        }

        public double getY2() {
            return y2;
        }

        public double getZ2() {
            return z2;
        }

        public double getRentAmount() {
            return rentAmount;
        }

        public void setRentAmount(double rentAmount) {
            this.rentAmount = rentAmount;
        }

        public UUID getOwnerUUID() {
            return ownerUUID;
        }

        public void setOwnerUUID(UUID ownerUUID) {
            this.ownerUUID = ownerUUID;
        }

        public List<UUID> getTenants() {
            return new ArrayList<>(tenants);
        }

        public void addTenant(UUID tenantUUID) {
            if (!tenants.contains(tenantUUID)) {
                tenants.add(tenantUUID);
            }
        }

        public void removeTenant(UUID tenantUUID) {
            tenants.remove(tenantUUID);
        }
    }
}