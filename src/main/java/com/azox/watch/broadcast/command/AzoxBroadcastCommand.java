package com.azox.watch.broadcast.command;

import com.azox.watch.AzoxWatchPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public final class AzoxBroadcastCommand implements CommandExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args == null || args.length == 0) {
            AzoxWatchPlugin.getInstance().reloadBroadcastConfig();
            sender.sendMessage("AzoxWatch broadcast configuration reloaded.");
            return true;
        }

        final String subCommand = args[0].toLowerCase(Locale.ENGLISH);
        if (!subCommand.equals("reload")) {
            sender.sendMessage("Usage: /" + label + " reload");
            return true;
        }

        AzoxWatchPlugin.getInstance().reloadBroadcastConfig();
        sender.sendMessage("AzoxWatch broadcast configuration reloaded.");
        return true;
    }
}
