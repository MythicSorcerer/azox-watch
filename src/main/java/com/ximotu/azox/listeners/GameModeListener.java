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

        String reason;
        
        // If it's a command, use the last stored command for this player
        if (event.getCause() == PlayerGameModeChangeEvent.Cause.COMMAND) {
            final String lastCommand = plugin.getLastCommands().get(player.getUniqueId());
            reason = lastCommand != null ? "[" + lastCommand + "]" : "[Command]";
        } else {
            // Infer F3 shortcuts
            if (newMode == GameMode.SPECTATOR || (oldMode == GameMode.SPECTATOR && newMode != GameMode.SPECTATOR)) {
                reason = "[F3-N]";
            } else {
                reason = "[F3-F4]";
            }
        }

        final String message = oldMode.name().toLowerCase() + " -> " + newMode.name().toLowerCase() + " " + reason;
        plugin.getLogManager().log(player, "[G]", message);
    }
}
