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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        this.formatAndAppendItemData(logBuilder, itemStack);

        plugin.getLogManager().log(player, "[T]", logBuilder.toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(@NotNull final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
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

        this.formatAndAppendItemData(logBuilder, itemStack);

        plugin.getLogManager().log(player, "[T]", logBuilder.toString());
    }

    /**
     * Attempts to format the item's component/NBT data into a more readable multiline format.
     */
    private void formatAndAppendItemData(final StringBuilder logBuilder, final ItemStack itemStack) {
        final String itemString = itemStack.toString();
        
        // 1.21 item toString typically looks like: Material[components...] or {Material x Count, META:{...}}
        // We'll look for content inside the first [ or {
        int startIndex = itemString.indexOf("[");
        if (startIndex == -1) {
            startIndex = itemString.indexOf("{");
            // If it starts with { it might be the {Material x Count, ...} format
            // Let's skip the Material x Count part if it matches
            int metaStart = itemString.indexOf("META:{");
            if (metaStart != -1) {
                startIndex = metaStart + 5;
            }
        }
        
        if (startIndex == -1) return;

        String data = itemString.substring(startIndex).trim();
        if (data.length() <= 2) return; // Empty [] or {}

        logBuilder.append("\n- Data:");
        
        // Simple heuristic: split by ", minecraft:" or ", " that is followed by a component name
        // This won't be perfect but will be much better than one massive line
        
        // Split components - look for ", " followed by something that looks like a component key (e.g. minecraft:lore)
        // or a meta key (e.g. internal=)
        final String[] parts = data.split(", (?=[a-zA-Z0-9_]+[:=])");
        
        for (String part : parts) {
            // Clean up wrapping brackets for each entry
            part = part.trim();
            if (part.startsWith("[") || part.startsWith("{")) part = part.substring(1);
            if (part.endsWith("]") || part.endsWith("}")) part = part.substring(0, part.length() - 1);
            
            if (!part.isEmpty()) {
                logBuilder.append("\n  * ").append(part);
            }
        }
    }
}
