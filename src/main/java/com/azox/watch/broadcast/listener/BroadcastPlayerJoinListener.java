package com.azox.watch.broadcast.listener;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.broadcast.service.BroadcastService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class BroadcastPlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (event == null) {
            return;
        }

        final Player player = event.getPlayer();
        final BroadcastService broadcastService = AzoxWatchPlugin.getInstance().getBroadcastService();
        if (broadcastService == null) {
            return;
        }

        broadcastService.handleJoin(player);
    }
}
