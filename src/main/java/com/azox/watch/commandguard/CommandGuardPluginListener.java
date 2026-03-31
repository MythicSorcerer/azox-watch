package com.azox.watch.commandguard;

import com.azox.watch.AzoxWatchPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

public final class CommandGuardPluginListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPluginDisable(final PluginDisableEvent event) {
        if (event == null || event.getPlugin() == null || event.getPlugin().getDescription() == null) {
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

        final String pluginName = event.getPlugin().getDescription().getName();
        service.handleProtectedPluginDisabled(pluginName);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event) {
        if (event == null) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || plugin.getCommandGuardService() == null) {
            return;
        }

        plugin.getCommandGuardService().clearExempt(event.getPlayer());
    }
}
