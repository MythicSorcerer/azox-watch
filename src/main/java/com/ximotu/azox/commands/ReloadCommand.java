package com.ximotu.azox.commands;

import com.ximotu.azox.AzoxWatch;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Command for reloading the plugin configuration using Paper's BasicCommand.
 */
public final class ReloadCommand implements BasicCommand {

    @Override
    public void execute(@NotNull final CommandSourceStack stack, @NotNull final String[] args) {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        final CommandSender sender = stack.getSender();
        if (plugin == null) {
            return;
        }

        if (!sender.hasPermission("azox.admin")) {
            final String noPermission = plugin.getConfig().getString("messages.no-permission", "&cYou do not have permission to execute this command.");
            this.sendMessage(sender, noPermission);
            return;
        }

        plugin.getConfigManager().reload();
        final String reloaded = plugin.getConfig().getString("messages.reload", "&aConfiguration reloaded successfully!");
        this.sendMessage(sender, reloaded);
    }

    private void sendMessage(final CommandSender sender, final String message) {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        final String prefix = plugin.getConfig().getString("settings.prefix", "");
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull final CommandSourceStack stack, @NotNull final String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean canUse(@NotNull final CommandSender sender) {
        return sender.hasPermission("azox.admin");
    }
}
