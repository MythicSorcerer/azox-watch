package com.azox.watch.commandguard;

import com.azox.watch.AzoxWatchPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class CommandGuardCommandListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onCommandLowest(final PlayerCommandPreprocessEvent event) {
        if (event == null) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        final Player player = event.getPlayer();
        final String rawCommand = event.getMessage();
        final CommandGuardService service = plugin.getCommandGuardService();
        if (service == null) {
            return;
        }

        if (!service.isDisallowedForPlayer(player, rawCommand)) {
            return;
        }

        event.setCancelled(true);
        service.handleBlockedPlayerCommand(player, rawCommand);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandMonitor(final PlayerCommandPreprocessEvent event) {
        if (event == null) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        final CommandGuardService service = plugin.getCommandGuardService();
        if (service == null) {
            return;
        }

        final Player player = event.getPlayer();
        final String rawCommand = event.getMessage();
        if (!service.isDisallowedForPlayer(player, rawCommand)) {
            return;
        }

        service.handleInterceptFailure(player, rawCommand);
    }
}
