package com.ximotu.azox;

import com.ximotu.azox.commands.AzCommand;
import com.ximotu.azox.commands.ReloadCommand;
import com.ximotu.azox.listeners.CommandListener;
import com.ximotu.azox.listeners.ConnectionListener;
import com.ximotu.azox.listeners.CreativeListener;
import com.ximotu.azox.listeners.GameModeListener;
import com.ximotu.azox.managers.BypassManager;
import com.ximotu.azox.managers.ConfigManager;
import com.ximotu.azox.managers.LogManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.logging.Level;

/**
 * Main plugin class for AzoxWatch.
 */
public final class AzoxWatch extends JavaPlugin {

    @Getter
    private static AzoxWatch instance;

    @Getter
    private ConfigManager configManager;

    @Getter
    private LogManager logManager;

    @Getter
    private BypassManager bypassManager;

    @Override
    public void onEnable() {
        // Set the singleton instance
        instance = this;

        // Save default configuration
        this.saveDefaultConfig();

        // Register Managers
        this.configManager = new ConfigManager();
        this.logManager = new LogManager();
        this.bypassManager = new BypassManager();

        this.getLogger().log(Level.INFO, "AzoxWatch has been enabled successfully.");

        // Register Commands using Paper Lifecycle API
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final io.papermc.paper.command.brigadier.Commands registrar = event.registrar();
            
            // Register /az command
            registrar.register(
                this.getPluginMeta(),
                "az",
                "Main command for AzoxWatch.",
                Collections.emptyList(),
                new AzCommand()
            );

            // Register /azoxreload command
            registrar.register(
                this.getPluginMeta(),
                "azoxreload",
                "Reload the plugin configuration.",
                Collections.emptyList(),
                new ReloadCommand()
            );
        });

        // Register Listeners
        this.getServer().getPluginManager().registerEvents(new ConnectionListener(), this);
        this.getServer().getPluginManager().registerEvents(new CommandListener(), this);
        this.getServer().getPluginManager().registerEvents(new GameModeListener(), this);
        this.getServer().getPluginManager().registerEvents(new CreativeListener(), this);
    }

    @Override
    public void onDisable() {
        if (this.logManager != null) {
            this.logManager.shutdown();
        }

        this.getLogger().log(Level.INFO, "AzoxWatch has been disabled.");
        
        // Clear instance on disable
        instance = null;
    }
}
