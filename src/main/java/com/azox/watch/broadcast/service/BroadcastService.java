package com.azox.watch.broadcast.service;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.broadcast.config.BroadcastConfig;
import com.azox.watch.broadcast.util.MiniMessageUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class BroadcastService {

    @Getter
    @Setter
    private BroadcastConfig broadcastConfig;

    private BukkitTask periodicTask;
    private final ConcurrentHashMap<UUID, Boolean> onceShown;
    private final DateTimeFormatter logTimeFormatter;

    public BroadcastService(final BroadcastConfig broadcastConfig) {
        this.broadcastConfig = broadcastConfig;
        this.onceShown = new ConcurrentHashMap<>();
        this.logTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    }

    public void startPeriodicTask() {
        this.stopPeriodicTask();

        if (this.broadcastConfig == null || this.broadcastConfig.getPeriodic() == null) {
            return;
        }

        final BroadcastConfig.BroadcastSection periodicSection = this.broadcastConfig.getPeriodic();
        if (!periodicSection.isEnabled()) {
            return;
        }

        final int intervalMinutes = periodicSection.getIntervalMinutes();
        if (intervalMinutes <= 0) {
            return;
        }

        final long intervalTicks = TimeUnit.MINUTES.toSeconds(intervalMinutes) * 20L;
        this.periodicTask = Bukkit.getScheduler().runTaskTimer(
                AzoxWatchPlugin.getInstance(),
                () -> this.broadcastToAll(periodicSection, "periodic"),
                intervalTicks,
                intervalTicks
        );
    }

    public void stopPeriodicTask() {
        if (this.periodicTask != null) {
            this.periodicTask.cancel();
            this.periodicTask = null;
        }
    }

    public void updateConfig(final BroadcastConfig config) {
        this.broadcastConfig = config;
        this.startPeriodicTask();
    }

    public void handleJoin(final Player player) {
        if (player == null || this.broadcastConfig == null) {
            return;
        }

        if (this.shouldWaitForAuthMeLogin()) {
            if (this.isAuthMeAuthenticated(player)) {
                this.scheduleJoinAndOnce(player);
            }
            return;
        }

        this.scheduleJoinAndOnce(player);
    }

    public void handleAuthMeLogin(final Player player) {
        if (player == null || this.broadcastConfig == null || !this.shouldWaitForAuthMeLogin()) {
            return;
        }

        this.scheduleJoinAndOnce(player);
    }

    private void scheduleJoinAndOnce(final Player player) {
        final BroadcastConfig.BroadcastSection joinSection = this.broadcastConfig.getJoin();
        if (joinSection != null && joinSection.isEnabled()) {
            this.scheduleSection(player, joinSection, "join");
        }

        final BroadcastConfig.BroadcastSection onceSection = this.broadcastConfig.getOnce();
        if (onceSection != null && onceSection.isEnabled()) {
            final UUID playerUniqueId = player.getUniqueId();
            if (!this.onceShown.containsKey(playerUniqueId)) {
                this.scheduleSection(player, onceSection, "once");
                this.onceShown.put(playerUniqueId, true);
            }
        }
    }

    private void scheduleSection(final Player player, final BroadcastConfig.BroadcastSection section, final String type) {
        if (player == null || section == null) {
            return;
        }

        final int safeDelaySeconds = Math.max(0, section.getDelaySeconds());
        final int delayBetweenSeconds = Math.max(0, section.getDelayBetweenSeconds());
        final List<BroadcastConfig.BroadcastMessage> messages = section.getMessages();
        if (messages == null || messages.isEmpty()) {
            return;
        }

        long totalDelayTicks = safeDelaySeconds * 20L;
        for (int index = 0; index < messages.size(); index++) {
            final BroadcastConfig.BroadcastMessage message = messages.get(index);
            if (!this.isTargetedPlayer(section, message, player)) {
                continue;
            }

            final int messageIndex = index + 1;
            final long scheduledDelay = totalDelayTicks;
            Bukkit.getScheduler().runTaskLater(
                    AzoxWatchPlugin.getInstance(),
                    () -> this.sendMessage(player, message, type, messageIndex),
                    scheduledDelay
            );

            totalDelayTicks += delayBetweenSeconds * 20L;
        }
    }

    private void sendMessage(final Player player, final BroadcastConfig.BroadcastMessage message, final String type, final int messageIndex) {
        if (player == null || message == null || !player.isOnline()) {
            return;
        }

        final List<String> displayLines = this.buildDisplayLines(message);
        if (displayLines.isEmpty()) {
            return;
        }

        for (final String line : displayLines) {
            player.sendMessage(MiniMessageUtil.deserialize(line));
        }

        this.logBroadcast(type, messageIndex, player, displayLines);
    }

    private void broadcastToAll(final BroadcastConfig.BroadcastSection section, final String type) {
        if (section == null) {
            return;
        }

        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.scheduleSection(onlinePlayer, section, type);
        }
    }

    private boolean shouldWaitForAuthMeLogin() {
        return this.broadcastConfig != null
                && this.broadcastConfig.isWaitForAuthMeLogin()
                && Bukkit.getPluginManager().isPluginEnabled("AuthMe");
    }

    private boolean isAuthMeAuthenticated(final Player player) {
        try {
            final Class<?> authMeApiClass = Class.forName("fr.xephi.authme.api.v3.AuthMeApi");
            final Object apiInstance = authMeApiClass.getMethod("getInstance").invoke(null);
            final Object result = authMeApiClass.getMethod("isAuthenticated", Player.class).invoke(apiInstance, player);
            return result instanceof Boolean && (Boolean) result;
        } catch (final Exception ignored) {
            return false;
        }
    }

    private boolean isTargetedPlayer(final BroadcastConfig.BroadcastSection section,
                                     final BroadcastConfig.BroadcastMessage message,
                                     final Player player) {
        if (section == null || player == null) {
            return false;
        }

        final String playerName = player.getName();
        final List<String> messageTargets = message == null ? null : message.getTargetPlayers();
        if (messageTargets != null && !messageTargets.isEmpty()) {
            return this.matchesTarget(messageTargets, playerName);
        }

        final List<String> sectionTargets = section.getTargetPlayers();
        if (sectionTargets == null || sectionTargets.isEmpty()) {
            return true;
        }

        return this.matchesTarget(sectionTargets, playerName);
    }

    private boolean matchesTarget(final List<String> targets, final String playerName) {
        if (targets == null || targets.isEmpty() || playerName == null) {
            return false;
        }

        for (final String targetPlayer : targets) {
            if (targetPlayer != null && targetPlayer.equalsIgnoreCase(playerName)) {
                return true;
            }
        }

        return false;
    }

    private List<String> buildDisplayLines(final BroadcastConfig.BroadcastMessage message) {
        final List<String> output = new ArrayList<>();
        if (message == null) {
            return output;
        }

        final List<String> lines = message.getLines();
        final String title = message.getTitle();
        final boolean hasTitle = title != null && !title.trim().isEmpty();

        int longestLineLength = 0;
        if (lines != null) {
            for (final String line : lines) {
                longestLineLength = Math.max(longestLineLength, this.getVisibleLength(line));
            }
        }

        if (hasTitle) {
            final int titleLength = this.getVisibleLength(title);
            int totalLength = Math.max(longestLineLength, titleLength + 2);
            final int headerMaxWidth = this.getHeaderMaxWidth();
            if (headerMaxWidth > 0) {
                if (this.isDividerMaxWidthEnabled()) {
                    totalLength = Math.max(titleLength + 2, headerMaxWidth);
                } else if (totalLength > headerMaxWidth) {
                    totalLength = Math.max(titleLength + 2, headerMaxWidth);
                }
            }

            int leftDashes = (totalLength - titleLength) / 2;
            int rightDashes = totalLength - titleLength - leftDashes;
            if (leftDashes < 1) {
                leftDashes = 1;
            }
            if (rightDashes < 1) {
                rightDashes = 1;
            }

            final String dividerColor = this.getDividerColor();
            output.add("<" + dividerColor + ">" + this.repeat("-", leftDashes) + title + this.repeat("-", rightDashes));
            if (lines != null) {
                output.addAll(lines);
            }
            output.add("<" + dividerColor + ">" + this.repeat("-", totalLength));
            return output;
        }

        if (lines != null) {
            output.addAll(lines);
        }

        return output;
    }

    private int getVisibleLength(final String line) {
        if (line == null) {
            return 0;
        }

        final Component component = MiniMessageUtil.deserialize(line);
        final String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        return plainText.length();
    }

    private String repeat(final String value, final int count) {
        if (value == null || count <= 0) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < count; index++) {
            stringBuilder.append(value);
        }
        return stringBuilder.toString();
    }

    private int getHeaderMaxWidth() {
        return this.broadcastConfig == null ? 50 : this.broadcastConfig.getHeaderMaxWidth();
    }

    private boolean isDividerMaxWidthEnabled() {
        return this.broadcastConfig == null || this.broadcastConfig.isDividerMaxWidth();
    }

    private String getDividerColor() {
        if (this.broadcastConfig == null) {
            return "blue";
        }

        final String dividerColor = this.broadcastConfig.getDividerColor();
        return dividerColor == null || dividerColor.trim().isEmpty() ? "blue" : dividerColor;
    }

    private void logBroadcast(final String type, final int messageIndex, final Player player, final List<String> lines) {
        if (player == null || type == null) {
            return;
        }

        final AzoxWatchPlugin plugin = AzoxWatchPlugin.getInstance();
        if (plugin == null || !plugin.getConfig().getBoolean("broadcast.log-broadcasts", false)) {
            return;
        }

        final String timestamp = this.logTimeFormatter.format(Instant.now());
        final String message = "[" + timestamp + "] type=" + type + " index=" + messageIndex
                + " player=" + player.getName() + " lines=" + lines.size() + System.lineSeparator();

        final Path logFilePath = plugin.getDataFolder().toPath().resolve("broadcast.log");
        try {
            Files.createDirectories(logFilePath.getParent());
            Files.write(logFilePath, message.getBytes(StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (final Exception ignored) {
        }
    }

    public void sendAdHoc(final Player player, final String message, final String color, final boolean raw) {
        if (player == null || message == null || message.trim().isEmpty() || !player.isOnline()) {
            return;
        }

        final BroadcastConfig.BroadcastMessage adHoc = this.createAdHocMessage(message, color, raw);
        final List<String> displayLines = this.buildDisplayLines(adHoc);
        if (displayLines.isEmpty()) {
            return;
        }

        for (final String line : displayLines) {
            player.sendMessage(MiniMessageUtil.deserialize(line));
        }

        this.logBroadcast("adhoc", 1, player, displayLines);
    }

    public void sendAdHocGlobal(final String message, final String color, final boolean raw) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            this.sendAdHoc(onlinePlayer, message, color, raw);
        }
    }

    private String applyColor(final String message, final String color) {
        if (message == null) {
            return "";
        }
        if (color == null || color.trim().isEmpty()) {
            return message;
        }
        return "<" + color + ">" + message + "</" + color + ">";
    }

    private BroadcastConfig.BroadcastMessage createAdHocMessage(final String message, final String color, final boolean raw) {
        final String title = "BROADCAST";
        final String finalMessage = raw ? message : this.applyColor(message, color);
        return new BroadcastConfig.BroadcastMessage(title, List.of(finalMessage), List.of());
    }
}
