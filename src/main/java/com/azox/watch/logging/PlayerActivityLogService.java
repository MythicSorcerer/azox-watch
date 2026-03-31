package com.azox.watch.logging;

import com.azox.watch.AzoxWatchPlugin;
import com.azox.watch.util.TimeFormats;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerActivityLogService {

    private static final Pattern SAFE_FILE_PATTERN = Pattern.compile("[^A-Za-z0-9._-]");
    private static final Pattern DATE_MARKER_PATTERN = Pattern.compile("^\\[(\\d{4}-\\d{2}-\\d{2})\\]$");

    private final AzoxWatchPlugin plugin;

    public PlayerActivityLogService(final AzoxWatchPlugin plugin) {
        this.plugin = plugin;
    }

    public void logCommand(final Player player, final String command) {
        if (player == null || command == null || command.isBlank()) {
            return;
        }
        this.append(player, List.of("[C] " + TimeFormats.nowTime() + ": " + command));
    }

    public void logModeChange(final Player player, final String from, final String to) {
        if (player == null || from == null || to == null) {
            return;
        }
        this.append(player, List.of("[G] " + TimeFormats.nowTime() + ": Mode: " + from + " -> " + to));
    }

    public void logCreativeTake(final Player player, final ItemStack itemStack, final String nbtText) {
        if (player == null || itemStack == null) {
            return;
        }

        final List<String> lines = new ArrayList<>();
        lines.add("[T] " + TimeFormats.nowTime() + ":");
        lines.add("- Taken: " + itemStack.getType().name());
        lines.add("- Amount: " + itemStack.getAmount());
        if (nbtText != null && !nbtText.isBlank()) {
            lines.add("- (If applicable): " + nbtText);
        }

        this.append(player, lines);
    }

    public void logCreativeDrop(final Player player, final ItemStack itemStack, final String nbtText) {
        if (player == null || itemStack == null) {
            return;
        }

        final List<String> lines = new ArrayList<>();
        lines.add("[D] " + TimeFormats.nowTime() + ":");
        lines.add("- Dropped: " + itemStack.getType().name());
        lines.add("- Amount: " + itemStack.getAmount());
        if (nbtText != null && !nbtText.isBlank()) {
            lines.add("- (If applicable): " + nbtText);
        }

        this.append(player, lines);
    }

    public void logSession(final Player player, final String action, final Location location, final String ipAddress) {
        if (player == null || action == null) {
            return;
        }

        final String coords;
        if (location == null) {
            coords = "(unknown)";
        } else {
            coords = String.format("(%.2f,%.2f,%.2f)", location.getX(), location.getY(), location.getZ());
        }

        final String ipSuffix = ipAddress == null || ipAddress.isBlank() ? "" : " IP: " + ipAddress;
        this.append(player, List.of("[L] " + TimeFormats.nowTime() + ": " + action + " " + coords + ipSuffix));
    }

    private synchronized void append(final Player player, final List<String> eventLines) {
        if (player == null || player.getUniqueId() == null || eventLines == null || eventLines.isEmpty()) {
            return;
        }

        final boolean perPlayerEnabled = this.plugin.getConfig().getBoolean("admin-log.logging.per-player.enabled", true);
        final boolean globalEnabled = this.plugin.getConfig().getBoolean("admin-log.logging.global.enabled", false);

        if (perPlayerEnabled) {
            final Path file = this.resolvePlayerFile(player);
            this.write(file, this.buildPlayerLines(player, eventLines));
        }

        if (globalEnabled) {
            final Path file = this.plugin.getCommandLogsDirectory().resolve("all-activity.log");
            this.write(file, this.buildGlobalLines(player, eventLines));
        }
    }

    private Path resolvePlayerFile(final Player player) {
        final String playerName = this.safeName(player.getName());
        final boolean useNameAndUuid = this.plugin.getConfig().getBoolean("admin-log.file-name.use-name-and-uuid", true);
        final String fileName = useNameAndUuid
                ? playerName + "_" + player.getUniqueId() + ".log"
                : player.getUniqueId() + ".log";

        return this.plugin.getCommandLogsDirectory().resolve(fileName);
    }

    private List<String> buildPlayerLines(final Player player, final List<String> eventLines) {
        final List<String> output = new ArrayList<>();
        final Path path = this.resolvePlayerFile(player);
        final String today = "[" + TimeFormats.today() + "]";

        final boolean fileExists = Files.exists(path);
        if (!fileExists) {
            output.add(player.getName() + " (" + player.getUniqueId() + ")");
        }

        final String lastDateMarker = this.readLastDateMarker(path);
        if (!today.equals(lastDateMarker)) {
            output.add(today);
        }
        output.addAll(eventLines);
        return output;
    }

    private List<String> buildGlobalLines(final Player player, final List<String> eventLines) {
        final List<String> output = new ArrayList<>();
        output.add("[" + TimeFormats.today() + "] " + player.getName() + " (" + player.getUniqueId() + ")");
        output.addAll(eventLines);
        return output;
    }

    private void write(final Path path, final List<String> lines) {
        if (path == null || lines == null || lines.isEmpty()) {
            return;
        }

        try {
            Files.createDirectories(path.getParent());
            final String payload = String.join(System.lineSeparator(), lines) + System.lineSeparator();
            Files.write(path, payload.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (final IOException exception) {
            this.plugin.getLogger().warning("Failed to write log file " + path.getFileName() + ": " + exception.getMessage());
        }
    }

    private String readLastDateMarker(final Path path) {
        if (path == null || !Files.exists(path)) {
            return null;
        }

        try {
            final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int index = lines.size() - 1; index >= 0; index--) {
                final String line = lines.get(index);
                if (line == null) {
                    continue;
                }
                final Matcher matcher = DATE_MARKER_PATTERN.matcher(line.trim());
                if (matcher.matches()) {
                    return line.trim();
                }
            }
        } catch (final IOException ignored) {
            return null;
        }

        return null;
    }

    private String safeName(final String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return SAFE_FILE_PATTERN.matcher(value).replaceAll("_");
    }
}
