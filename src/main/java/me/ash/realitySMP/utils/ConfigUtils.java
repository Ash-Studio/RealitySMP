package me.ash.realitySMP.utils;

import me.ash.realitySMP.RealitySMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtils {
    private static RealitySMP plugin;
    private static Map<String, FileConfiguration> configs = new HashMap<>();

    public static void initialize(RealitySMP instance) {
        plugin = instance;
    }

    /**
     * Creates or loads a configuration file.
     *
     * @param fileName The name of the file (without .yml extension)
     * @return The FileConfiguration object for the file
     */
    public static FileConfiguration getConfig(String fileName) {
        if (configs.containsKey(fileName)) {
            return configs.get(fileName);
        }

        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create " + fileName + ".yml file: " + e.getMessage());
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
        return config;
    }

    /**
     * Saves a configuration file.
     *
     * @param fileName The name of the file (without .yml extension)
     */
    public static void saveConfig(String fileName) {
        if (!configs.containsKey(fileName)) {
            return;
        }

        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        try {
            configs.get(fileName).save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + fileName + ".yml file: " + e.getMessage());
        }
    }

    /**
     * Reload a configuration file from disk.
     *
     * @param fileName The name of the file (without .yml extension)
     * @return The reloaded FileConfiguration
     */
    public static FileConfiguration reloadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            return getConfig(fileName);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(fileName, config);
        return config;
    }

    /**
     * Sets a default value in a configuration if the path doesn't exist.
     *
     * @param fileName The name of the file (without .yml extension)
     * @param path The configuration path
     * @param value The default value
     */
    public static void setDefault(String fileName, String path, Object value) {
        FileConfiguration config = getConfig(fileName);
        if (!config.contains(path)) {
            config.set(path, value);
            saveConfig(fileName);
        }
    }

    /**
     * Gets a value from a configuration with a default fallback.
     *
     * @param fileName The name of the file (without .yml extension)
     * @param path The configuration path
     * @param defaultValue The default value if path doesn't exist
     * @return The value at the path, or the default value
     */
    public static Object get(String fileName, String path, Object defaultValue) {
        FileConfiguration config = getConfig(fileName);
        if (config.contains(path)) {
            return config.get(path);
        }
        return defaultValue;
    }

    /**
     * Creates a folder if it doesn't exist
     *
     * @param folderName Name of the folder to create
     * @return True if folder exists or was created successfully
     */
    public static boolean createFolder(String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);
        if (!folder.exists()) {
            return folder.mkdirs();
        }
        return true;
    }

    /**
     * Saves all loaded configurations to disk.
     */
    public static void saveAll() {
        for (String fileName : configs.keySet()) {
            saveConfig(fileName);
        }
    }
}