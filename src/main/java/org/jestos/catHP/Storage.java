package org.jestos.catHP;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Storage {
    private File file;
    private FileConfiguration config;

    public Storage(String fileName) {
        file = new File(CatHP.getInstance().getDataFolder(), fileName);
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException(fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save", e);
        }
    }
}
