package org.jestos.catHP.command;

import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
            CatHP.getInstance().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded");
            return;
        }

        if (args[0].equalsIgnoreCase("buy")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be run by a player.");
            } else {
                int currentHp = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".hp");
                int maxHp = CatHP.getInstance().getConfig().getInt("max-hp");
                if (currentHp <= maxHp) {
                    sender.sendMessage(CatHP.getInstance().getConfig().getString("messages.buy-max-hp"));
                }
                sender.sendMessage(Color.RED + "buy");

                double amount = CatHP.getUsersStorage().getConfig().getInt(player.getUniqueId() + ".buy");
                EconomyResponse r = CatHP.getEconomy().withdrawPlayer(player, amount);
                if(r.transactionSuccess()) {
                    sender.sendMessage(String.format("You were given %s and now have %s", CatHP.getEconomy().format(r.amount), CatHP.getEconomy().format(r.balance)));
                } else {
                    sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
                }
            }

            return;
        }

        if (args[0].equalsIgnoreCase("set")) {
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
        return Lists.newArrayList();
    }
}
