package com.azox.watch.swear;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.util.TimeFormats;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class SwearLogService {

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9_\\-]");

    private final AzoxWatchPlugin plugin;

    public SwearLogService(final AzoxWatchPlugin plugin) {
        this.plugin = plugin;
    }

    public void appendBlocked(final Player player, final String message) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }

        final Path file = this.resolveLogFile(player);
        try {
            final Path logsDirectory = this.resolveLogsDirectory();
            Files.createDirectories(logsDirectory);
            final boolean hasContent = Files.exists(file) && Files.size(file) > 0;

            final List<String> lines = new ArrayList<>();
            if (!hasContent) {
                lines.add(player.getName() + " (" + player.getUniqueId() + ")");
            }

            lines.add("[" + TimeFormats.today() + "]");
            lines.add("[B] " + TimeFormats.nowTime() + ": " + message);

            final String payload = String.join(System.lineSeparator(), lines) + System.lineSeparator();
            Files.write(file, payload.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (final IOException ignored) {
        }
    }

    private Path resolveLogFile(final Player player) {
        final String safeName = safeName(player.getName());
        return this.resolveLogsDirectory().resolve(safeName + "_chat_" + player.getUniqueId() + ".log");
    }

    private Path resolveLogsDirectory() {
        if (this.plugin == null) {
            return Path.of("chat-logs");
        }
        return this.plugin.getChatLogsDirectory();
    }

    private static String safeName(final String name) {
        if (name == null || name.isBlank()) {
            return "Unknown";
        }
        return SAFE_NAME_PATTERN.matcher(name).replaceAll("_");
    }
}
