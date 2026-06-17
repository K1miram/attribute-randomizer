package com.kimiram.attributerandomizer;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class ConfigUtil {
    private final File file;
    private final FileConfiguration config;
    private List<AttributeRandomizer.Attribute> loadedAttributes;

    public ConfigUtil(File file) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        config.set("isEnabled", AttributeRandomizer.isEnabled);
        config.set("mode", AttributeRandomizer.mode.toString().toLowerCase().replace('_', '-'));
        config.set("attributes", null);
        for (AttributeRandomizer.Attribute attribute: loadedAttributes) {
            config.set("attributes." + attribute.id() + "." + "min_value", attribute.minValue());
            config.set("attributes." + attribute.id() + "." + "max_value", attribute.maxValue());
            config.set("attributes." + attribute.id() + "." + "default_value", attribute.defaultValue());
        }
        config.set("period", AttributeRandomizer.period);
        config.set("amount", AttributeRandomizer.amount);
        config.set("sendChangesToPlayers", AttributeRandomizer.sendChangesToPlayers);

        try {
            config.save(file);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Could not save attribute randomizer config to " + file.getName() + ": " + e);
        }
    }

    public void readConfig() {
        AttributeRandomizer.isEnabled = config.getBoolean("isEnabled", AttributeRandomizer.isEnabled);
        AttributeRandomizer.mode = readMode(config.getString("mode"), AttributeRandomizer.mode);
        AttributeRandomizer.attributes = readAttributes();
        AttributeRandomizer.period = config.getLong("period", AttributeRandomizer.period);
        AttributeRandomizer.amount = config.getInt("amount", AttributeRandomizer.amount);
        AttributeRandomizer.sendChangesToPlayers = config.getBoolean(
                "sendChangesToPlayers",
                AttributeRandomizer.sendChangesToPlayers
        );
    }

    private AttributeRandomizer.Mode readMode(String value, AttributeRandomizer.Mode fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            return AttributeRandomizer.Mode.valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Unknown attribute randomizer mode in config: " + value);
            return fallback;
        }
    }

    private List<AttributeRandomizer.Attribute> readAttributes() {
        ConfigurationSection attributesSection = config.getConfigurationSection("attributes");
        if (attributesSection == null) {
            loadedAttributes = List.copyOf(AttributeRandomizer.DEFAULT_ATTRIBUTES);
            return List.copyOf(AttributeRandomizer.DEFAULT_ATTRIBUTES);
        }

        List<AttributeRandomizer.Attribute> attributes = new ArrayList<>();
        for (String key : attributesSection.getKeys(false)) {
            String path = key + ".";
            attributes.add(new AttributeRandomizer.Attribute(
                    key,
                    attributesSection.getDouble(path + "min_value"),
                    attributesSection.getDouble(path + "max_value"),
                    attributesSection.getDouble(path + "default_value")
            ));
        }

        loadedAttributes = List.copyOf(attributes);

        return verifyAttributes(attributes);
    }

    public static List<AttributeRandomizer.Attribute> verifyAttributes(List<AttributeRandomizer.Attribute> attrs) {
        List<AttributeRandomizer.Attribute> attributes = new ArrayList<>();
        for (AttributeRandomizer.Attribute attribute: attrs) {
            try {
                Registry.ATTRIBUTE.getOrThrow(NamespacedKey.fromString(attribute.id()));
                attributes.add(attribute);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Unknown attribute with id " + attribute.id());
            }
        }
        return attributes;
    }
}
