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
            sender.sendMessage("Usage: /" + label + " reload");
            sender.sendMessage("Usage: /" + label + " buy");
            sender.sendMessage("Usage: /" + label + " set <player> <hp>");
            sender.sendMessage("Usage: /" + label + " reset <player>");
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("cat-hp.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return;
            }
            CatHP.getInstance().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded");
            return;
        }

        if (args[0].equalsIgnoreCase("buy")) {
            if (!sender.hasPermission("cat-hp.buy")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be run by a player.");
            } else {
                int currentHp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
                int maxHp = CatHP.getInstance().getConfig().getInt("max-hp");
                if (currentHp >= maxHp) {
                    String messageMaxHp = CatHP.getInstance().getConfig().getString("messages.buy-max-hp");
                    if (messageMaxHp != null) {
                        sender.sendMessage(messageMaxHp);
                    }
                    return;
                }

                double amount = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".buy");
                double playerBal = CatHP.getEconomy().getBalance(player);
                if (playerBal < amount) {
                    String messageSuccess = CatHP.getInstance().getConfig().getString("messages.not-money");
                    if (messageSuccess != null) {
                        sender.sendMessage(messageSuccess);
                    }
                    return;
                }

                EconomyResponse r = CatHP.getEconomy().withdrawPlayer(player, amount);

                if(r.transactionSuccess()) {
                    CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".hp", currentHp + 1);
                    CatHP.getUsersStorage().getConfig().set(player.getUniqueId() + ".buy", amount * 2);
                    CatHP.getUsersStorage().save();
                    String messageSuccess = CatHP.getInstance().getConfig().getString("messages.success-buy");
                    if (messageSuccess != null) {
                        messageSuccess = messageSuccess.replace("{amount}", String.valueOf(amount));
                        sender.sendMessage(messageSuccess);
                    }
                } else {
                    String messageSuccess = CatHP.getInstance().getConfig().getString("messages.failed-buy");
                    if (messageSuccess != null) {
                        messageSuccess = messageSuccess.replace("{error}", r.errorMessage);
                        sender.sendMessage(messageSuccess);
                    }
                }
            }

            return;
        }

        if (args[0].equalsIgnoreCase("set")) {
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
                    target.setGameMode(GameMode.SPECTATOR);
                }
                else if (hp > 0 && target.getGameMode() == GameMode.SPECTATOR) {
                    target.setGameMode(GameMode.SURVIVAL);
                    Location spawnLocation = target.getWorld().getSpawnLocation();
                    target.teleport(spawnLocation);
                }
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s HP to " + hp);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid HP value");
            }
        }

        if (args[0].equalsIgnoreCase("reset")) {
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
                target.setGameMode(GameMode.SURVIVAL);
                Location spawnLocation = target.getWorld().getSpawnLocation();
                target.teleport(spawnLocation);
            }
            sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s HP to " + maxHp);
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
