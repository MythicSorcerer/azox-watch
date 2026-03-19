package com.ximotu.azox.listeners;

import com.ximotu.azox.AzoxWatch;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener for player command events.
 */
@NoArgsConstructor
public final class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(@NotNull final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String command = event.getMessage();
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin == null) {
            return;
        }

        // Log the command
        plugin.getLogManager().log(player, "[C]", command);

        // Store last command for gamemode tracking
        plugin.getLastCommands().put(player.getUniqueId(), command);

        // Check blacklist
        if (plugin.getBypassManager().isBypassed(player.getUniqueId())) {
            return;
        }

        final List<String> blacklist = plugin.getConfigManager().getBlacklistedCommands();
        final String commandLower = command.toLowerCase();
        
        for (final String blacklisted : blacklist) {
            if (commandLower.startsWith(blacklisted.toLowerCase())) {
                event.setCancelled(true);
                final String deniedMessage = plugin.getConfig().getString("messages.denied", "&cYou cannot execute this command.");
                final String prefix = plugin.getConfig().getString("settings.prefix", "");
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + deniedMessage));
                break;
            }
        }
    }
}
