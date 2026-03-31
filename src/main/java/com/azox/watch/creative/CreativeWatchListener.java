package com.azox.watch.creative;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.logging.PlayerActivityLogService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public final class CreativeWatchListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onModeChange(final PlayerGameModeChangeEvent event) {
        if (event == null) {
            return;
        }

        final Player player = event.getPlayer();
        if (!this.shouldLogPlayer(player)) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("creative-watch.log-gamemode-changes", true)) {
            return;
        }

        final PlayerActivityLogService service = plugin.getPlayerActivityLogService();
        if (service != null && player.getGameMode() != null && event.getNewGameMode() != null) {
            service.logModeChange(player, player.getGameMode().name(), event.getNewGameMode().name());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreativeTake(final InventoryCreativeEvent event) {
        if (event == null) {
            return;
        }

        final HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player player)) {
            return;
        }

        if (!this.shouldLogPlayer(player)) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("creative-watch.log-creative-takes", true)) {
            return;
        }

        final InventoryType inventoryType = event.getClickedInventory() == null ? null : event.getClickedInventory().getType();
        final ClickType clickType = event.getClick();
        if (inventoryType != InventoryType.PLAYER || clickType != ClickType.CREATIVE) {
            return;
        }

        final ItemStack item = event.getCursor();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        final PlayerActivityLogService service = plugin.getPlayerActivityLogService();
        if (service != null) {
            service.logCreativeTake(player, item, this.extractNbt(item));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreativeDrop(final PlayerDropItemEvent event) {
        if (event == null) {
            return;
        }

        final Player player = event.getPlayer();
        if (!this.shouldLogPlayer(player)) {
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("creative-watch.log-creative-drops", true)) {
            return;
        }

        final ItemStack dropped = event.getItemDrop() == null ? null : event.getItemDrop().getItemStack();
        if (dropped == null || dropped.getType() == Material.AIR) {
            return;
        }

        final PlayerActivityLogService service = plugin.getPlayerActivityLogService();
        if (service != null) {
            service.logCreativeDrop(player, dropped, this.extractNbt(dropped));
        }
    }

    private boolean shouldLogPlayer(final Player player) {
        if (player == null || player.getUniqueId() == null) {
            return false;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("creative-watch.enabled", true)) {
            return false;
        }

        final List<String> exempt = plugin.getConfig().getStringList("creative-watch.exempt-players");
        if (exempt == null || exempt.isEmpty()) {
            return true;
        }

        final UUID uuid = player.getUniqueId();
        final String uuidText = uuid.toString();
        final String name = player.getName();
        for (final String entry : exempt) {
            if (entry == null) {
                continue;
            }
            if (entry.equalsIgnoreCase(uuidText) || entry.equalsIgnoreCase(name)) {
                return false;
            }
        }

        return true;
    }

    private String extractNbt(final ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return null;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        if (!itemMeta.hasDisplayName()
                && !itemMeta.hasLore()
                && !itemMeta.hasEnchants()
                && !itemMeta.hasAttributeModifiers()
                && !itemMeta.hasCustomModelData()
                && itemMeta.getPersistentDataContainer().isEmpty()) {
            return null;
        }

        return itemMeta.serialize().toString();
    }
}
