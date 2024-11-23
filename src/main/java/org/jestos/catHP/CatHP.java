package org.jestos.catHP;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jestos.catHP.command.CatHPCommand;
import java.util.logging.Logger;

public final class CatHP extends JavaPlugin {
    private static CatHP instance;
    private static Logger log;
    private Storage usersStorage;

    @Override
    public void onEnable() {
        instance = this;
        log = this.getLogger();
        saveDefaultConfig();
        usersStorage = new Storage("users.yml");
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CatHPPlaceholderExpansion(this).register();
        }

        new CatHPCommand();

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static CatHP getInstance() {
        return instance;
    }

    public static Storage getUsersStorage() {
        return instance.usersStorage;
    }

    public static Logger logger() {
        return log;
    }
}
