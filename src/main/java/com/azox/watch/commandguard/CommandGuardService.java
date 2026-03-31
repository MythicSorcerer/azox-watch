package com.azox.watch.commandguard;

import com.azox.watch.AzoxWatchPlugin;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandGuardService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AzoxWatchPlugin plugin;
    private final Set<UUID> exemptPlayers = ConcurrentHashMap.newKeySet();
    private volatile boolean shutdownTriggered;

    public CommandGuardService(final AzoxWatchPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean handleBlockedPlayerCommand(final Player player, final String rawCommand) {
        if (!this.isEnabled() || player == null || rawCommand == null) {
            return false;
        }

        if (this.isExempt(player)) {
            return false;
        }

        final String normalized = normalizeCommand(rawCommand);
        if (normalized.isBlank()) {
            return false;
        }

        if (this.containsAny(normalized, this.getKickCommands())) {
            this.kickPlayer(player, rawCommand);
            return true;
        }

        if (this.containsAny(normalized, this.getBanCommands())) {
            this.banPlayer(player, rawCommand);
            return true;
        }

        return false;
    }

    public boolean isDisallowedForPlayer(final Player player, final String rawCommand) {
        if (!this.isEnabled() || player == null || rawCommand == null || this.isExempt(player)) {
            return false;
        }

        final String normalized = normalizeCommand(rawCommand);
        return this.containsAny(normalized, this.getKickCommands()) || this.containsAny(normalized, this.getBanCommands());
    }

    public void handleInterceptFailure(final Player player, final String rawCommand) {
        if (!this.isEnabled() || !this.shouldStopServerOnFail()) {
            return;
        }

        final String playerName = player == null ? "unknown" : player.getName();
        this.plugin.getLogger().severe("Command guard intercept failure detected. player=" + playerName + " command=" + rawCommand);
        this.triggerSecurityShutdown("command_intercept_failure", player, rawCommand);
    }

    public void handleProtectedPluginDisabled(final String disabledPluginName) {
        if (!this.isEnabled() || !this.shouldStopServerOnFail()) {
            return;
        }
        if (Bukkit.isStopping()) {
            return;
        }

        final String normalizedName = disabledPluginName == null ? "" : disabledPluginName.toLowerCase(Locale.ENGLISH);
        if (!this.getProtectedPlugins().contains(normalizedName)) {
            return;
        }

        this.plugin.getLogger().severe("Protected plugin disabled: " + disabledPluginName + ". Triggering security shutdown.");
        this.triggerSecurityShutdown("protected_plugin_disabled", null, disabledPluginName);
    }

    public boolean tryExempt(final Player player, final String code) {
        if (player == null || code == null) {
            return false;
        }

        final String expected = this.plugin.getConfig().getString("command-guard.exempt-code", "83427196");
        if (expected == null || expected.isBlank()) {
            return false;
        }

        if (!code.trim().equals(expected.trim())) {
            return false;
        }

        this.exemptPlayers.add(player.getUniqueId());
        return true;
    }

    public void clearExempt(final Player player) {
        if (player == null || player.getUniqueId() == null) {
            return;
        }
        this.exemptPlayers.remove(player.getUniqueId());
    }

    public boolean isExempt(final Player player) {
        return player != null && player.getUniqueId() != null && this.exemptPlayers.contains(player.getUniqueId());
    }

    private void banPlayer(final Player player, final String rawCommand) {
        final String reason = this.plugin.getConfig().getString("command-guard.ban-reason", "Disallowed command attempt");
        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, "AzoxWatch");
        player.kickPlayer(reason);
        this.plugin.getLogger().severe("Command guard banned player " + player.getName() + " for command: " + rawCommand);
    }

    private void kickPlayer(final Player player, final String rawCommand) {
        final String reason = this.plugin.getConfig().getString("command-guard.kick-reason", "Disallowed command attempt");
        player.kickPlayer(reason);
        this.plugin.getLogger().warning("Command guard kicked player " + player.getName() + " for command: " + rawCommand);
    }

    private void triggerSecurityShutdown(final String trigger, final Player player, final String detail) {
        if (this.shutdownTriggered) {
            return;
        }
        this.shutdownTriggered = true;

        this.writeNoRestartMarker(trigger, player, detail);

        final String shutdownMessage = this.plugin.getConfig().getString(
                "command-guard.shutdown-message",
                "[AzoxWatch] Security shutdown triggered."
        );
        if (shutdownMessage != null && !shutdownMessage.isBlank()) {
            Bukkit.broadcastMessage(shutdownMessage);
        }

        Bukkit.shutdown();
    }

    private void writeNoRestartMarker(final String trigger, final Player player, final String detail) {
        final boolean markerEnabled = this.plugin.getConfig().getBoolean("command-guard.no-restart-marker.enabled", true);
        if (!markerEnabled) {
            return;
        }

        final String fileName = this.plugin.getConfig().getString("command-guard.no-restart-marker.file-name", "AZOXWATCH_NO_RESTART.flag");
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        final Path markerPath = this.plugin.getServer().getWorldContainer().toPath().resolve(fileName);
        final List<String> lines = new ArrayList<>();
        lines.add("trigger=" + trigger);
        lines.add("time=" + LocalDateTime.now().format(TIME_FORMATTER));
        if (player != null) {
            lines.add("player=" + player.getName());
            lines.add("uuid=" + player.getUniqueId());
        }
        if (detail != null && !detail.isBlank()) {
            lines.add("detail=" + detail);
        }

        try {
            final String content = String.join("\n", lines) + "\n";
            Files.write(markerPath, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (final IOException exception) {
            this.plugin.getLogger().warning("Failed to write no-restart marker file: " + exception.getMessage());
        }
    }

    private boolean isEnabled() {
        return this.plugin.getConfig().getBoolean("command-guard.enabled", true);
    }

    private boolean shouldStopServerOnFail() {
        return this.plugin.getConfig().getBoolean("command-guard.stop-server-on-fail", true);
    }

    private List<String> getKickCommands() {
        return this.plugin.getConfig().getStringList("command-guard.kick-commands");
    }

    private List<String> getBanCommands() {
        return this.plugin.getConfig().getStringList("command-guard.ban-commands");
    }

    private Set<String> getProtectedPlugins() {
        final List<String> raw = this.plugin.getConfig().getStringList("command-guard.protected-plugins");
        final Set<String> out = ConcurrentHashMap.newKeySet();
        for (final String entry : raw) {
            if (entry != null && !entry.isBlank()) {
                out.add(entry.trim().toLowerCase(Locale.ENGLISH));
            }
        }
        out.add("azoxwatch");
        return out;
    }

    private boolean containsAny(final String normalizedCommand, final List<String> patterns) {
        if (normalizedCommand == null || normalizedCommand.isBlank() || patterns == null || patterns.isEmpty()) {
            return false;
        }

        for (final String rawPattern : patterns) {
            if (rawPattern == null || rawPattern.isBlank()) {
                continue;
            }
            final String pattern = normalizeCommand(rawPattern);
            if (!pattern.isBlank() && normalizedCommand.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    public static String normalizeCommand(final String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String normalized = raw.trim().toLowerCase(Locale.ENGLISH);
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }
}
