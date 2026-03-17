package com.ximotu.azox.listeners;

import com.ximotu.azox.AzoxWatch;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for player connection events.
 */
@NoArgsConstructor
public final class ConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin != null) {
            plugin.getLogManager().logSessionStart(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin != null) {
            final String coords = plugin.getLogManager().formatLocation(player.getLocation());
            final String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown";
            final String message = "Disconnected [Coordinates: " + coords + "] [IP: " + ip + "]";
            plugin.getLogManager().log(player, "[L]", message);
            
            // Clear bypass status
            plugin.getBypassManager().remove(player.getUniqueId());
        }
    }
}
