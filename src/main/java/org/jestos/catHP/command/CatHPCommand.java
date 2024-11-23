package org.jestos.catHP.command;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            CatHP.getInstance().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded");
            return;
        }

        if (args[0].equalsIgnoreCase("buy")) {
            sender.sendMessage(ChatColor.GREEN + "buy");
            return;
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Lists.newArrayList("reload", "buy");
        }
        return Lists.newArrayList();
    }
}
