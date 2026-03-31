package com.azox.watch.session;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.logging.PlayerActivityLogService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class SessionListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event == null ? null : event.getPlayer();
        this.logSession(player, "Login", this.extractIp(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        this.logSession(event == null ? null : event.getPlayer(), "Logout", null);
    }

    private void logSession(final Player player, final String action, final String ipAddress) {
        if (player == null) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("admin-log.enabled", true)) {
            return;
        }

        if (!plugin.getConfig().getBoolean("admin-log.log-login-logout", true)) {
            return;
        }

        final PlayerActivityLogService service = plugin.getPlayerActivityLogService();
        if (service != null) {
            service.logSession(player, action, player.getLocation(), ipAddress);
        }
    }

    private String extractIp(final Player player) {
        if (player == null || player.getAddress() == null || player.getAddress().getAddress() == null) {
            return null;
        }
        return player.getAddress().getAddress().getHostAddress();
    }
}
