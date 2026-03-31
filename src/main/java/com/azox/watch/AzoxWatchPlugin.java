package com.azox.watch;

import com.azox.watch.admin.AdminLogListener;
import com.azox.watch.command.AzCommand;
import com.azox.watch.commandguard.CommandGuardCommandListener;
import com.azox.watch.commandguard.CommandGuardPluginListener;
import com.azox.watch.commandguard.CommandGuardService;
import com.azox.watch.broadcast.command.AzoxBroadcastCommand;
import com.azox.watch.broadcast.command.AzoxQuickBroadcastCommand;
import com.azox.watch.broadcast.config.BroadcastConfig;
import com.azox.watch.broadcast.listener.AuthMeLoginListener;
import com.azox.watch.broadcast.listener.BroadcastPlayerJoinListener;
import com.azox.watch.broadcast.service.BroadcastService;
import com.azox.watch.creative.CreativeWatchListener;
import com.azox.watch.logging.PlayerActivityLogService;
import com.azox.watch.session.SessionListener;
import com.azox.watch.swear.FilterSettings;
import com.azox.watch.swear.SwearChatListener;
import com.azox.watch.swear.SwearFilterService;
import com.azox.watch.swear.SwearLogService;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;

public final class AzoxWatchPlugin extends JavaPlugin {

    @Getter
    private static AzoxWatchPlugin instance;

    @Getter
    private PlayerActivityLogService playerActivityLogService;

    @Getter
    private BroadcastService broadcastService;

    @Getter
    private FilterSettings filterSettings;

    @Getter
    private SwearFilterService swearFilterService;

    @Getter
    private SwearLogService swearLogService;

    @Getter
    private CommandGuardService commandGuardService;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.ensureFolders();

        this.playerActivityLogService = new PlayerActivityLogService(this);
        this.commandGuardService = new CommandGuardService(this);
        this.reloadBroadcastConfig();

        this.filterSettings = FilterSettings.fromConfig(this.getConfig());
        this.swearFilterService = SwearFilterService.fromSettings(this.filterSettings);
        this.swearLogService = new SwearLogService(this);

        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new AdminLogListener(), this);
        pluginManager.registerEvents(new SessionListener(), this);
        pluginManager.registerEvents(new CreativeWatchListener(), this);
        pluginManager.registerEvents(new CommandGuardCommandListener(), this);
        pluginManager.registerEvents(new CommandGuardPluginListener(), this);
        pluginManager.registerEvents(new BroadcastPlayerJoinListener(), this);
        pluginManager.registerEvents(new SwearChatListener(), this);

        if (pluginManager.isPluginEnabled("AuthMe")) {
            new AuthMeLoginListener().register(this);
        }

        if (this.getCommand("azoxbroadcast") != null) {
            this.getCommand("azoxbroadcast").setExecutor(new AzoxBroadcastCommand());
        }
        if (this.getCommand("azbc") != null) {
            this.getCommand("azbc").setExecutor(new AzoxQuickBroadcastCommand(false));
        }
        if (this.getCommand("azbcc") != null) {
            this.getCommand("azbcc").setExecutor(new AzoxQuickBroadcastCommand(true));
        }
        if (this.getCommand("az") != null) {
            final AzCommand azCommand = new AzCommand();
            this.getCommand("az").setExecutor(azCommand);
            this.getCommand("az").setTabCompleter(azCommand);
        }
    }

    @Override
    public void onDisable() {
        if (this.broadcastService != null) {
            this.broadcastService.stopPeriodicTask();
        }
        this.commandGuardService = null;
        instance = null;
    }

    public void reloadBroadcastConfig() {
        this.reloadConfig();
        this.ensureFolders();
        final BroadcastConfig broadcastConfig = BroadcastConfig.fromConfig(this.getConfig());
        if (this.broadcastService == null) {
            this.broadcastService = new BroadcastService(broadcastConfig);
        } else {
            this.broadcastService.updateConfig(broadcastConfig);
        }
        this.broadcastService.startPeriodicTask();

        this.filterSettings = FilterSettings.fromConfig(this.getConfig());
        this.swearFilterService = SwearFilterService.fromSettings(this.filterSettings);
    }

    private void ensureFolders() {
        if (!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
            this.getLogger().warning("Failed to create plugin data folder.");
        }

        if (this.useUnifiedLogsFolder()) {
            final File unifiedLogsFolder = this.getUnifiedLogsDirectory().toFile();
            if (!unifiedLogsFolder.exists() && !unifiedLogsFolder.mkdirs()) {
                this.getLogger().warning("Failed to create unified logs folder.");
            }
            return;
        }

        final File commandLogsFolder = this.getCommandLogsDirectory().toFile();
        if (!commandLogsFolder.exists() && !commandLogsFolder.mkdirs()) {
            this.getLogger().warning("Failed to create command-logs folder.");
        }

        final File chatLogsFolder = this.getChatLogsDirectory().toFile();
        if (!chatLogsFolder.exists() && !chatLogsFolder.mkdirs()) {
            this.getLogger().warning("Failed to create chat-logs folder.");
        }
    }

    public Path getCommandLogsDirectory() {
        if (this.useUnifiedLogsFolder()) {
            return this.getUnifiedLogsDirectory();
        }
        return this.getDataFolder().toPath().resolve("command-logs");
    }

    public Path getChatLogsDirectory() {
        if (this.useUnifiedLogsFolder()) {
            return this.getUnifiedLogsDirectory();
        }
        return this.getDataFolder().toPath().resolve("chat-logs");
    }

    private Path getUnifiedLogsDirectory() {
        final String configuredName = this.getConfig().getString("storage.unified-folder-name", "logs");
        final String folderName = configuredName == null || configuredName.isBlank() ? "logs" : configuredName.trim();
        return this.getDataFolder().toPath().resolve(folderName);
    }

    private boolean useUnifiedLogsFolder() {
        return this.getConfig().getBoolean("storage.use-unified-logs-folder", false);
    }
}
