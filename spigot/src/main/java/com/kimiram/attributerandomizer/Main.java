package com.kimiram.attributerandomizer;

import com.kimiram.attributerandomizer.command.ARTabCompleter;
import com.kimiram.attributerandomizer.command.AttributeRandomizerCommand;
import com.kimiram.attributerandomizer.listener.OnPlayerDeath;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {
    public static Main INSTANCE;
    public ConfigUtil config;

    @Override
    public void onEnable() {
        getCommand("attribute-randomizer").setTabCompleter(new ARTabCompleter());
        getCommand("attribute-randomizer").setExecutor(new AttributeRandomizerCommand());
        getServer().getPluginManager().registerEvents(new OnPlayerDeath(), this);
        INSTANCE = this;

        config = new ConfigUtil(new File("config/attribute-randomizer.yml"));

        config.readConfig();
        AttributeRandomizer.updateDoChange();

        if (AttributeRandomizer.isEnabled) {
            AttributeRandomizer.startRandomizer();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        config.saveConfig();
    }
}
