package org.jestos.catHP;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

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
                String message = CatHP.getInstance().getConfig().getString("messages.lost-all-hp");
                message = message.replace("%player%", player.getName());
                Bukkit.broadcastMessage(message);
            } else {
                CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", playerHp - 1);
                String message = CatHP.getInstance().getConfig().getString("messages.lost-one-hp");
                message = message.replace("%player%", player.getName());
                Bukkit.broadcastMessage(message);
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
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}
