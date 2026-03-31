package com.azox.watch.broadcast.listener;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.broadcast.service.BroadcastService;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

public final class AuthMeLoginListener implements Listener {

    public void register(final AzoxWatchPlugin plugin) {
        if (plugin == null) {
            return;
        }

        final Class<? extends Event> loginEventClass = this.findAuthMeLoginEvent();
        if (loginEventClass == null) {
            plugin.getLogger().warning("AuthMe detected but login event class was not found. Login waiting will be ignored.");
            return;
        }

        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(
                loginEventClass,
                this,
                EventPriority.NORMAL,
                (listener, event) -> this.onAuthMeLogin(event),
                plugin
        );
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Event> findAuthMeLoginEvent() {
        try {
            return (Class<? extends Event>) Class.forName("fr.xephi.authme.events.LoginEvent");
        } catch (final ClassNotFoundException ignored) {
            return null;
        }
    }

    private void onAuthMeLogin(final Event event) {
        if (event == null) {
            return;
        }

        final Player player = this.extractPlayer(event);
        if (player == null) {
            return;
        }

        final BroadcastService broadcastService = AzoxWatchPlugin.getInstance().getBroadcastService();
        if (broadcastService == null) {
            return;
        }

        broadcastService.handleAuthMeLogin(player);
    }

    private Player extractPlayer(final Event event) {
        try {
            final Method getPlayerMethod = event.getClass().getMethod("getPlayer");
            final Object result = getPlayerMethod.invoke(event);
            if (result instanceof Player) {
                return (Player) result;
            }
        } catch (final ReflectiveOperationException ignored) {
            return null;
        }

        return null;
    }
}
