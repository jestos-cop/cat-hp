package org.jestos.catHP.command;

import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jestos.catHP.CatHP;

import java.util.List;
import java.util.Objects;

public class CatHPCommand extends AbstractCommand {
    public CatHPCommand() {
        super("cat-hp");
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "buy":
                handleBuy(sender);
                break;
            case "set":
                handleSet(sender, label, args);
                break;
            case "reset":
                handleReset(sender, label, args);
                break;
            case "resetbuy":
                handleResetBuy(sender, label, args);
                break;
            case "info":
                handleInfo(sender);
                break;
            default:
                sendUsage(sender, label);
                break;
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sendMessage(sender, "messages.usage-reload", label);
        sendMessage(sender, "messages.usage-buy", label);
        sendMessage(sender, "messages.usage-set", label);
        sendMessage(sender, "messages.usage-reset", label);
        sendMessage(sender, "messages.usage-resetbuy", label);
        sendMessage(sender, "messages.usage-info", label);
    }

    private void handleInfo(CommandSender sender) {
        if (!sender.hasPermission("cat-hp.info")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.player-only");
            return;
        }
        int currentHp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
        int maxHp = CatHP.getInstance().getConfig().getInt("max-hp");
        int buyPrice = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".buy");

        sendMessage(sender, "messages.info", currentHp, maxHp, buyPrice);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("cat-hp.reload")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }
        CatHP.getInstance().reloadConfig();
        sendMessage(sender, "messages.config-reloaded");
    }

    private void handleBuy(CommandSender sender) {
        if (!sender.hasPermission("cat-hp.buy")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.player-only");
            return;
        }
        int currentHp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
        int maxHp = CatHP.getInstance().getConfig().getInt("max-hp");
        if (currentHp >= maxHp) {
            sendMessage(sender, "messages.buy-max-hp");
            return;
        }

        double amount = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".buy");
        double playerBal = CatHP.getEconomy().getBalance(player);
        if (playerBal < amount) {
            sendMessage(sender, "messages.not-money", amount);
            return;
        }

        EconomyResponse r = CatHP.getEconomy().withdrawPlayer(player, amount);

        if (r.transactionSuccess()) {
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", currentHp + 1);
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".buy", amount * 2);
            CatHP.getUsersStorage().save();
            sendMessage(sender, "messages.success-buy", amount);
            if (currentHp == 0) {
                setGameModeAndTeleportToSpawn(player, GameMode.SURVIVAL);
            }
        } else {
            sendMessage(sender, "messages.failed-buy", r.errorMessage);
        }
    }

    private void handleResetBuy(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("cat-hp.resetbuy")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }
        if (args.length != 2) {
            sendMessage(sender, "messages.usage-resetbuy", label);
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, "messages.player-not-found");
            return;
        }
        double startPrice = CatHP.getInstance().getConfig().getDouble("start-price");
        CatHP.getUsersStorage().getConfig().set(target.getUniqueId() + ".buy", startPrice);
        CatHP.getUsersStorage().save();
        sendMessage(sender, "messages.reset-buy-price", target.getName(), startPrice);
    }

    private void handleSet(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("cat-hp.set")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }
        if (args.length != 3) {
            sendMessage(sender, "messages.usage-set", label);
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, "messages.player-not-found");
            return;
        }
        try {
            int hp = Integer.parseInt(args[2]);
            if (hp < 0) {
                hp = 0;
            }
            CatHP.getUsersStorage().getConfig().set(target.getUniqueId() + ".hp", hp);
            CatHP.getUsersStorage().save();
            if (hp == 0 && target.getGameMode() != GameMode.SPECTATOR) {
                setGameModeAndTeleportToSpawn(target, GameMode.SPECTATOR);
            } else if (hp > 0 && target.getGameMode() == GameMode.SPECTATOR) {
                setGameModeAndTeleportToSpawn(target, GameMode.SURVIVAL);
            }
            sendMessage(sender, "messages.set-hp", target.getName(), hp);
        } catch (NumberFormatException e) {
            sendMessage(sender, "messages.invalid-hp");
        }
    }

    private void handleReset(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("cat-hp.reset")) {
            sendMessage(sender, "messages.no-permission");
            return;
        }
        if (args.length != 2) {
            sendMessage(sender, "messages.usage-reset", label);
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, "messages.player-not-found");
            return;
        }
        int maxHp = CatHP.getInstance().getConfig().getInt("max-hp");
        CatHP.getUsersStorage().getConfig().set(target.getUniqueId() + ".hp", maxHp);
        CatHP.getUsersStorage().save();
        if (target.getGameMode() == GameMode.SPECTATOR) {
            setGameModeAndTeleportToSpawn(target, GameMode.SURVIVAL);
        }
        sendMessage(sender, "messages.reset-hp", target.getName(), maxHp);
    }

    private void setGameModeAndTeleportToSpawn(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);
        if (gameMode == GameMode.SURVIVAL) {
            Location spawnLocation = Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation();
            player.teleport(spawnLocation);
        }
    }

    private void sendMessage(CommandSender sender, String configPath) {
        String message = CatHP.getInstance().getConfig().getString(configPath);
        if (message != null) {
            sender.sendMessage(message);
        }
    }

    private void sendMessage(CommandSender sender, String configPath, Object... args) {
        String message = CatHP.getInstance().getConfig().getString(configPath);
        if (message != null) {
            message = String.format(message, args);
            sender.sendMessage(message);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Lists.newArrayList("reload", "buy", "set", "reset", "resetbuy", "info");
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "set":
                case "reset":
                case "resetbuy":
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                default:
                    return Lists.newArrayList();
            }
        }
        return Lists.newArrayList();
    }
}
