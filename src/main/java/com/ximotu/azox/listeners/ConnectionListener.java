package com.ximotu.azox.listeners;

import com.ximotu.azox.AzoxWatch;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for player connection and world change events.
 */
@NoArgsConstructor
public final class ConnectionListener implements Listener {

    private static final String FAIRPLAY_CODE = "\u00A7f\u00A7a\u00A7i\u00A7r\u00A7x\u00A7a\u00A7e\u00A7r\u00A7o";
    private static final String DISABLED_CODE = "\u00A7n\u00A7o\u00A7m\u00A7i\u00A7n\u00A7i\u00A7m\u00A7a\u00A7p";

    @EventHandler
    public void onPlayerJoin(@NotNull final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin != null) {
            plugin.getLogManager().logSessionStart(player);
            this.sendMapControlCode(player);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(@NotNull final PlayerChangedWorldEvent event) {
        this.sendMapControlCode(event.getPlayer());
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

    /**
     * Sends the appropriate map control code to the player based on their current world.
     *
     * @param player The player to send the code to.
     */
    private void sendMapControlCode(final Player player) {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin == null) return;

        final String worldName = player.getWorld().getName();
        final String mode = plugin.getConfigManager().getMapMode(worldName);

        if (mode == null) return;

        if (mode.equalsIgnoreCase("fairplay")) {
            player.sendRawMessage(FAIRPLAY_CODE);
        } else if (mode.equalsIgnoreCase("disabled")) {
            player.sendRawMessage(DISABLED_CODE);
        }
    }
}
