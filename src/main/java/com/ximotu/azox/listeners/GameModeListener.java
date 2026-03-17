package com.ximotu.azox.listeners;

import com.ximotu.azox.AzoxWatch;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for player gamemode change events.
 */
@NoArgsConstructor
public final class GameModeListener implements Listener {

    @EventHandler
    public void onGameModeChange(@NotNull final PlayerGameModeChangeEvent event) {
        final Player player = event.getPlayer();
        final GameMode oldMode = player.getGameMode();
        final GameMode newMode = event.getNewGameMode();
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin == null) {
            return;
        }

        String reason = "[" + event.getCause().name() + "]";
        
        // Infer F3 shortcuts if cause is not COMMAND
        if (event.getCause() != PlayerGameModeChangeEvent.Cause.COMMAND) {
            if (newMode == GameMode.SPECTATOR || (oldMode == GameMode.SPECTATOR && newMode != GameMode.SPECTATOR)) {
                reason = "[F3-N]";
            } else {
                reason = "[F3-F4]";
            }
        } else {
            // If it's a command, we don't necessarily know WHICH command here
            // but the user's example shows [/gm s]
            // We can't easily get the exact command here without tracking it.
            // I'll just use a generic marker as a fallback.
            reason = "[Command]";
        }

        final String message = oldMode.name().toLowerCase() + " -> " + newMode.name().toLowerCase() + " " + reason;
        plugin.getLogManager().log(player, "[G]", message);
    }
}
