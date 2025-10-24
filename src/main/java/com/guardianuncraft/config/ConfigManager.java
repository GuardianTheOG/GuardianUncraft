package com.guardianuncraft.config;

import com.guardianuncraft.GuardianUncraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {
    private final GuardianUncraft plugin;
    private FileConfiguration config;
    private FileConfiguration recipesConfig;
    private File recipesFile;

    private final Map<String, Boolean> allowedMap = new HashMap<>();
    private final Map<String, List<String>> customIngredients = new HashMap<>();
    private final Map<String, Integer> customResultAmount = new HashMap<>();

    public ConfigManager(GuardianUncraft plugin) {
        this.plugin = plugin;
        loadConfig();
        loadRecipes();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void loadRecipes() {
        recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            plugin.saveResource("recipes.yml", false);
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);

        allowedMap.clear();
        customIngredients.clear();
        customResultAmount.clear();

        for (String key : recipesConfig.getKeys(false)) {
            ConfigurationSection section = recipesConfig.getConfigurationSection(key);
            if (section == null) continue;

            boolean allowed = section.getBoolean("allowed", true);
            allowedMap.put(key.toUpperCase(Locale.ROOT), allowed);

            if (section.contains("ingredients")) {
                List<String> ingredients = section.getStringList("ingredients");
                customIngredients.put(key.toUpperCase(Locale.ROOT), ingredients);
            }

            if (section.contains("result")) {
                int result = section.getInt("result", 1);
                customResultAmount.put(key.toUpperCase(Locale.ROOT), result);
            }
        }
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfig();
        loadRecipes();
        plugin.getLogger().info("Guardian-Uncraft configuration and recipes reloaded.");
    }

    public boolean isAllowed(String material) {
        return allowedMap.getOrDefault(material.toUpperCase(Locale.ROOT), true);
    }

    public List<String> getCustomIngredients(String material) {
        return customIngredients.get(material.toUpperCase(Locale.ROOT));
    }

    public Integer getCustomResultAmount(String material) {
        return customResultAmount.get(material.toUpperCase(Locale.ROOT));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getRecipesConfig() {
        return recipesConfig;
    }

    public void saveRecipes() {
        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
