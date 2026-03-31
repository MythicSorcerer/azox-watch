package com.azox.watch.broadcast.command;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.broadcast.service.BroadcastService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AzoxQuickBroadcastCommand implements CommandExecutor {

    private final boolean withColor;

    public AzoxQuickBroadcastCommand(final boolean withColor) {
        this.withColor = withColor;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        final List<String> arguments = new ArrayList<>(List.of(args));
        final boolean raw = this.removeFlag(arguments, "--raw");
        if (arguments.isEmpty()) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        final String mode = arguments.get(0).toLowerCase(Locale.ENGLISH);
        if (!mode.equals("i") && !mode.equals("g")) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        final BroadcastService service = AzoxWatchPlugin.getInstance().getBroadcastService();
        if (service == null) {
            sender.sendMessage("Broadcast service not available.");
            return true;
        }

        if (mode.equals("i")) {
            return this.handleIndividual(sender, label, arguments, service, raw);
        }

        return this.handleGlobal(sender, label, arguments, service, raw);
    }

    private boolean handleIndividual(final CommandSender sender,
                                     final String label,
                                     final List<String> args,
                                     final BroadcastService service,
                                     final boolean raw) {
        if (args.size() < (this.withColor ? 4 : 3)) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        final String targetName = args.get(1);
        final Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage("Player not found: " + targetName);
            return true;
        }

        final String color = this.withColor ? args.get(2) : "";
        final int messageStartIndex = this.withColor ? 3 : 2;
        final String message = this.joinMessage(args, messageStartIndex);
        if (message.isEmpty()) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        service.sendAdHoc(target, message, color, raw);
        sender.sendMessage("Sent broadcast to " + target.getName() + ".");
        return true;
    }

    private boolean handleGlobal(final CommandSender sender,
                                 final String label,
                                 final List<String> args,
                                 final BroadcastService service,
                                 final boolean raw) {
        if (args.size() < (this.withColor ? 3 : 2)) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        final String color = this.withColor ? args.get(1) : "";
        final int messageStartIndex = this.withColor ? 2 : 1;
        final String message = this.joinMessage(args, messageStartIndex);
        if (message.isEmpty()) {
            sender.sendMessage(this.getUsage(label));
            return true;
        }

        service.sendAdHocGlobal(message, color, raw);
        sender.sendMessage("Sent broadcast to all players.");
        return true;
    }

    private String joinMessage(final List<String> args, final int startIndex) {
        if (args == null || startIndex >= args.size()) {
            return "";
        }

        final StringBuilder out = new StringBuilder();
        for (int i = startIndex; i < args.size(); i++) {
            if (i > startIndex) {
                out.append(' ');
            }
            out.append(args.get(i));
        }
        return out.toString().trim();
    }

    private String getUsage(final String label) {
        if (this.withColor) {
            return "Usage: /" + label + " <i|g> (target) <color> <message>";
        }
        return "Usage: /" + label + " <i|g> (target) <message>";
    }

    private boolean removeFlag(final List<String> args, final String flag) {
        if (args == null || flag == null) {
            return false;
        }

        for (int i = 0; i < args.size(); i++) {
            if (flag.equalsIgnoreCase(args.get(i))) {
                args.remove(i);
                return true;
            }
        }

        return false;
    }
}
