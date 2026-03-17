package com.ximotu.azox.commands;

import com.ximotu.azox.AzoxWatch;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Main command for AzoxWatch using Paper's BasicCommand.
 */
public final class AzCommand implements BasicCommand {

    @Override
    public void execute(@NotNull final CommandSourceStack stack, @NotNull final String[] args) {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        final CommandSender sender = stack.getSender();
        if (plugin == null) {
            return;
        }

        if (args.length == 0) {
            this.sendInfo(sender, plugin);
            return;
        }

        if (args[0].equalsIgnoreCase("info")) {
            this.sendInfo(sender, plugin);
            return;
        }

        // Check if it's a 4-digit bypass code
        if (sender instanceof final Player player) {
            final String bypassCode = plugin.getConfigManager().getBypassCode();
            if (args[0].equals(bypassCode)) {
                plugin.getBypassManager().add(player.getUniqueId());
                this.sendMessage(player, plugin.getConfig().getString("messages.bypassed", "&aBypass successfully activated."));
            } else {
                this.sendInfo(sender, plugin);
            }
        }
    }

    private void sendInfo(final CommandSender sender, final AzoxWatch plugin) {
        final String version = plugin.getPluginMeta().getVersion();
        final String message = plugin.getConfig().getString("messages.info", "&bAzoxWatch &7v%version% &7- Recording actions.")
                .replace("%version%", version);
        this.sendMessage(sender, message);
    }

    private void sendMessage(final CommandSender sender, final String message) {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        final String prefix = plugin.getConfig().getString("settings.prefix", "");
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull final CommandSourceStack stack, @NotNull final String[] args) {
        if (args.length <= 1) {
            return Collections.singletonList("info");
        }
        return Collections.emptyList();
    }
}
