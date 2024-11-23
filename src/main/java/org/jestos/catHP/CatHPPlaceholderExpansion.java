package org.jestos.catHP;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CatHPPlaceholderExpansion extends PlaceholderExpansion {

    private final CatHP plugin;

    public CatHPPlaceholderExpansion(CatHP plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cathp";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("hp")) {
            return String.valueOf(CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId().toString() + ".hp"));
        }

        if (identifier.equals("hp_max")) {
            return String.valueOf(CatHP.getInstance().getConfig().getInt("max-hp"));
        }

        return null;
    }
}