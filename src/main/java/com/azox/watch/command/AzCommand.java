package com.azox.watch.command;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.commandguard.CommandGuardService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AzCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null) {
            return true;
        }

        final String infoLine = plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion();
        if (args == null || args.length == 0) {
            sender.sendMessage(infoLine);
            return true;
        }

        final String sub = args[0].toLowerCase(Locale.ENGLISH);
        if (sub.equals("info")) {
            sender.sendMessage(infoLine);
            return true;
        }

        if (sub.equals("exempt")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(infoLine);
                return true;
            }

            if (!player.hasPermission("azoxwatch.guard.exempt")) {
                sender.sendMessage(infoLine);
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("Usage: /" + label + " exempt <code>");
                return true;
            }

            final CommandGuardService service = plugin.getCommandGuardService();
            if (service != null && service.tryExempt(player, args[1])) {
                player.sendMessage("AzoxWatch guard exempt enabled for this session.");
            } else {
                player.sendMessage("Invalid exempt code.");
            }
            return true;
        }

        sender.sendMessage(infoLine);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args == null || args.length == 0) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            final String typed = args[0] == null ? "" : args[0].toLowerCase(Locale.ENGLISH);
            if ("info".startsWith(typed)) {
                return List.of("info");
            }
        }

        return Collections.emptyList();
    }
}
