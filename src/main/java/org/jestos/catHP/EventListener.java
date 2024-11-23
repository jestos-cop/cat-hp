package org.jestos.catHP;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        int hp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
        if (hp <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();
        List<String> worlds = CatHP.getInstance().getConfig().getStringList("worlds");

        if (worlds.contains(toWorld)) {
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".previous-gamemode", player.getGameMode().name());
            player.setGameMode(GameMode.ADVENTURE);
        } else if (worlds.contains(fromWorld)) {
            String previousGameMode = CatHP.getUsersStorage().getConfig().getString(player.getUniqueId() + ".previous-gamemode", GameMode.SURVIVAL.name());
            player.setGameMode(GameMode.valueOf(previousGameMode));
        }
        CatHP.getUsersStorage().save();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerConfig = CatHP.getUsersStorage().getConfig().getString(player.getUniqueId().toString());
        CatHP.logger().info("Player config: " + playerConfig);
        if (playerConfig == null) {
            int maxHp = CatHP.getInstance().getConfig().getInt("default-hp");
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", maxHp);
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".buy", 250);
            CatHP.getUsersStorage().save();
        }
        checkAndSetSpectatorMode(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Integer playerHp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
        if (playerHp != null) {
            if (playerHp <= 1) {
                CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", 0);
                sendMessage("messages.lost-all-hp", player.getName());
            } else {
                CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", playerHp - 1);
                sendMessage("messages.lost-one-hp", player.getName());
            }
            CatHP.getUsersStorage().save();
            checkAndSetSpectatorMode(player);
        }
    }

    private void checkAndSetSpectatorMode(Player player) {
        int playerHp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
        if (playerHp < 0) {
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", 0);
            playerHp = 0;
        }
        if (playerHp == 0 && player.getGameMode() != GameMode.SPECTATOR) {
            setGameModeAndGroup(player, GameMode.SPECTATOR);
        }
    }

    private void setGameModeAndGroup(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);
    }

    private void sendMessage(String configPath, String playerName) {
        String message = CatHP.getInstance().getConfig().getString(configPath);
        if (message != null) {
            message = message.replace("%player%", playerName);
            Bukkit.broadcastMessage(message);
        }
    }
}
