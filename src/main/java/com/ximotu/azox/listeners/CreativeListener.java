package com.ximotu.azox.listeners;

import com.ximotu.azox.AzoxWatch;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Listener for player taking items from or dropping items from creative inventory.
 */
@NoArgsConstructor
public final class CreativeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreativeInventoryClick(@NotNull final InventoryCreativeEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final ItemStack itemStack = event.getCursor();
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin == null || itemStack.getType().isAir()) {
            return;
        }

        final StringBuilder logBuilder = new StringBuilder();
        
        // Detect if item is dropped directly from inventory screen (click outside)
        final String action = (event.getSlot() == -999) ? "Dropped (Direct)" : "Taken";

        logBuilder.append(":\n")
                .append("- ").append(action).append(": ").append(itemStack.getType().name().replace("_", " ")).append("\n")
                .append("- Amount: ").append(itemStack.getAmount());

        this.appendNbtData(logBuilder, itemStack);

        plugin.getLogManager().log(player, "[T]", logBuilder.toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(@NotNull final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        // Only log "creative drops" here if they are in creative mode 
        // to distinguish from survival drops if necessary, 
        // but the prompt implies items taken/dropped from creative mode.
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }

        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin == null) {
            return;
        }

        final ItemStack itemStack = event.getItemDrop().getItemStack();
        final StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(":\n")
                .append("- Dropped (Hotbar): ").append(itemStack.getType().name().replace("_", " ")).append("\n")
                .append("- Amount: ").append(itemStack.getAmount());

        this.appendNbtData(logBuilder, itemStack);

        plugin.getLogManager().log(player, "[T]", logBuilder.toString());
    }

    private void appendNbtData(final StringBuilder logBuilder, final ItemStack itemStack) {
        final String itemString = itemStack.toString();
        if (itemString.contains("{") || itemString.contains("[")) {
            int startIndex = itemString.indexOf("[");
            if (startIndex == -1) {
                startIndex = itemString.indexOf("{");
            }
            
            if (startIndex != -1) {
                final String components = itemString.substring(startIndex);
                logBuilder.append("\n- NBT: ").append(components);
            }
        }
    }
}
