package org.jestos.catHP.command;

import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jestos.catHP.CatHP;

import java.util.List;

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
            default:
                sendUsage(sender, label);
                break;
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("Usage: /" + label + " reload");
        sender.sendMessage("Usage: /" + label + " buy");
        sender.sendMessage("Usage: /" + label + " set <player> <hp>");
        sender.sendMessage("Usage: /" + label + " reset <player>");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("cat-hp.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        CatHP.getInstance().reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded");
    }

    private void handleBuy(CommandSender sender) {
        if (!sender.hasPermission("cat-hp.buy")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
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
            sendMessage(sender, "messages.not-money");
            return;
        }

        EconomyResponse r = CatHP.getEconomy().withdrawPlayer(player, amount);

        if (r.transactionSuccess()) {
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", currentHp + 1);
            CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".buy", amount * 2);
            CatHP.getUsersStorage().save();
            sendMessage(sender, "messages.success-buy", amount);
            if (currentHp == 0) {
                setGameModeAndGroup(player, GameMode.SURVIVAL);
                Location spawnLocation = player.getWorld().getSpawnLocation();
                player.teleport(spawnLocation);
            }
        } else {
            sendMessage(sender, "messages.failed-buy", r.errorMessage);
        }
    }

    private void handleSet(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("cat-hp.set")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " set <player> <hp>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
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
                setGameModeAndGroup(target, GameMode.SPECTATOR);
            } else if (hp > 0 && target.getGameMode() == GameMode.SPECTATOR) {
                setGameModeAndGroup(target, GameMode.SURVIVAL);
                Location spawnLocation = target.getWorld().getSpawnLocation();
                target.teleport(spawnLocation);
            }
            sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s HP to " + hp);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid HP value");
        }
    }

    private void handleReset(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("cat-hp.reset")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reset <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }
        int maxHp = CatHP.getInstance().getConfig().getInt("max-hp");
        CatHP.getUsersStorage().getConfig().set(target.getUniqueId() + ".hp", maxHp);
        CatHP.getUsersStorage().save();
        if (target.getGameMode() == GameMode.SPECTATOR) {
            setGameModeAndGroup(target, GameMode.SURVIVAL);
            Location spawnLocation = target.getWorld().getSpawnLocation();
            target.teleport(spawnLocation);
        }
        sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s HP to " + maxHp);
    }

    private void setGameModeAndGroup(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);
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
            return Lists.newArrayList("reload", "buy", "set", "reset");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return Lists.newArrayList();
    }
}
