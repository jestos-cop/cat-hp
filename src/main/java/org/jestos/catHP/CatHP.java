package org.jestos.catHP;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jestos.catHP.command.CatHPCommand;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;


public final class CatHP extends JavaPlugin {
    private static CatHP instance;
    private static Logger log;
    private static Economy economy;
    private Storage usersStorage;

    @Override
    public void onEnable() {
        instance = this;
        log = this.getLogger();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log.info("LOL");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log.info("rsp null");
            return false;
        }
        log.info("privider null");
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return instance.economy;
    }


}
