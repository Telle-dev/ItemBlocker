package com.tellegram.itemblocker.commands;

import com.tellegram.itemblocker.ItemBlockerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ItemBlockCommand implements TabExecutor {

    private final ItemBlockerPlugin plugin;

    public ItemBlockCommand(ItemBlockerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <reload|check <ITEM>|scan [player]>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload":
                plugin.reloadSettings();
                sender.sendMessage("ItemBlocker configuration reloaded.");
                return true;
            case "check":
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " check <ITEM>");
                    return true;
                }
                String name = args[1].toUpperCase(Locale.ROOT);
                try {
                    Material m = Material.valueOf(name);
                    boolean blocked = plugin.isBlocked(m);
                    sender.sendMessage("Item " + m.name() + " is " + (blocked ? "BLOCKED" : "ALLOWED") + ".");
                } catch (IllegalArgumentException ex) {
                    sender.sendMessage("Unknown material: " + name);
                }
                return true;
            case "scan":

                if (args.length >= 2 && args[1].equals("*")) {
                    int count = 0;
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        plugin.scanPlayerInventories(online, true);
                        count++;
                    }
                    sender.sendMessage("Scanned inventories for all online players (" + count + ").");
                    return true;
                }

                Player target;
                if (args.length >= 2) {
                    target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage("Player not found: " + args[1]);
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                } else {
                    sender.sendMessage("Usage: /" + label + " scan <player|*>");
                    return true;
                }
                plugin.scanPlayerInventories(target, true);
                sender.sendMessage("Scanned inventories for " + target.getName() + ".");
                return true;
            default:
                sender.sendMessage("Unknown subcommand: " + sub);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "check", "scan");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("scan")) {
            String prefix = args[1];
            List<String> names = new ArrayList<>();

            if ("*".startsWith(prefix)) names.add("*");

            for (Player p : Bukkit.getOnlinePlayers()) {
                String n = p.getName();
                if (prefix == null || prefix.isEmpty() || n.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                    names.add(n);
                }
            }
            return names;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("check")) {
            String prefix = args[1].toUpperCase(Locale.ROOT);
            List<String> options = new ArrayList<>();
            for (Material m : Material.values()) {
                if (m.name().startsWith(prefix)) options.add(m.name());
            }
            return options;
        }
        return List.of();
    }
}
