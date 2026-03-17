package com.ximotu.azox.managers;

import com.ximotu.azox.AzoxWatch;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Manager for asynchronous file logging.
 */
public final class LogManager {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final DecimalFormat locFormat = new DecimalFormat("#.##");
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor();
    private final File logsFolder;

    public LogManager() {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        this.logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!this.logsFolder.exists()) {
            this.logsFolder.mkdirs();
        }
    }

    /**
     * Logs the start of a session (Login).
     */
    public void logSessionStart(final Player player) {
        final String playerName = player.getName();
        final String playerUuid = player.getUniqueId().toString();
        final String dateStr = this.dateFormat.format(new Date());
        final String timestamp = this.timeFormat.format(new Date());
        final String coords = this.formatLocation(player.getLocation());
        final String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown";

        this.logExecutor.submit(() -> {
            final File logFile = new File(this.logsFolder, playerName + "_" + playerUuid + ".log");
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(playerName + " [" + playerUuid + "]");
                writer.newLine();
                writer.newLine();
                writer.write("[" + dateStr + "]");
                writer.newLine();
                writer.write("[L] [" + timestamp + "] Login [Coordinates: " + coords + "] [IP: " + ip + "]");
                writer.newLine();
            } catch (final IOException exception) {
                AzoxWatch.getInstance().getLogger().log(Level.SEVERE, "Could not write to log file for " + playerName, exception);
            }
        });
    }

    /**
     * Logs a message for a player with a tag.
     *
     * @param player  The player.
     * @param tag     The tag (e.g., [C], [G], [T]).
     * @param message The message to log.
     */
    public void log(final Player player, final String tag, final String message) {
        final String playerName = player.getName();
        final String playerUuid = player.getUniqueId().toString();
        final String timestamp = this.timeFormat.format(new Date());
        
        this.logExecutor.submit(() -> {
            final File logFile = new File(this.logsFolder, playerName + "_" + playerUuid + ".log");
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(tag + " [" + timestamp + "] " + message);
                writer.newLine();
            } catch (final IOException exception) {
                AzoxWatch.getInstance().getLogger().log(Level.SEVERE, "Could not write to log file for " + playerName, exception);
            }
        });
    }

    public String formatLocation(final Location location) {
        return this.locFormat.format(location.getX()) + " " +
               this.locFormat.format(location.getY()) + " " +
               this.locFormat.format(location.getZ());
    }

    /**
     * Shuts down the log executor.
     */
    public void shutdown() {
        this.logExecutor.shutdown();
    }
}
