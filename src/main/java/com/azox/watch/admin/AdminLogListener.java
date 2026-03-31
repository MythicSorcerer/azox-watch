package com.azox.watch.admin;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.logging.PlayerActivityLogService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class AdminLogListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        if (event == null) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("admin-log.enabled", true)) {
            return;
        }

        if (!plugin.getConfig().getBoolean("admin-log.log-commands", true)) {
            return;
        }

        final Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        final String command = event.getMessage();
        if (command == null || command.isBlank()) {
            return;
        }

        final PlayerActivityLogService service = plugin.getPlayerActivityLogService();
        if (service != null) {
            service.logCommand(player, command);
        }
    }
}
