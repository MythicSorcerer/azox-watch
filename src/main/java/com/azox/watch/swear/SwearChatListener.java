package com.azox.watch.swear;

import com.azox.watch.AzoxWatchPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class SwearChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncChat(final AsyncChatEvent event) {
        if (event == null) {
            return;
        }

        final Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        final Component messageComponent = event.message();
        if (messageComponent == null) {
            return;
        }

        final String message = PlainTextComponentSerializer.plainText().serialize(messageComponent);
        if (message == null || message.isBlank()) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null) {
            return;
        }

        final SwearFilterService filterService = plugin.getSwearFilterService();
        final SwearLogService logService = plugin.getSwearLogService();
        final FilterSettings settings = plugin.getFilterSettings();
        if (filterService == null || logService == null || settings == null) {
            return;
        }

        if (!filterService.isMessageBlocked(message)) {
            return;
        }

        event.setCancelled(true);
        if (settings.isLogChat()) {
            logService.appendBlocked(player, message);
        }

        final String broadcastMessage = formatBlockMessage(settings.getBlockMessage(), player.getName());
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().broadcastMessage(broadcastMessage));
    }

    private static String formatBlockMessage(final String template, final String playerName) {
        final String safeTemplate = template == null || template.isBlank()
                ? "[Azo] Stopped $playername from saying a bad word."
                : template;

        final String safePlayerName = playerName == null ? "Unknown" : playerName;
        return safeTemplate.replace("$playername", safePlayerName);
    }
}
