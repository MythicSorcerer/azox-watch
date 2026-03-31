package com.azox.watch.broadcast.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public final class BroadcastConfig {

    private boolean waitForAuthMeLogin;
    private int headerMaxWidth;
    private boolean dividerMaxWidth;
    private String dividerColor;
    private BroadcastSection join;
    private BroadcastSection once;
    private BroadcastSection periodic;

    public static BroadcastConfig fromConfig(final FileConfiguration fileConfiguration) {
        if (fileConfiguration == null) {
            return new BroadcastConfig(false, 50, true, "blue",
                    new BroadcastSection(false, 0, 0, 5, List.of(), List.of()),
                    new BroadcastSection(false, 0, 0, 5, List.of(), List.of()),
                    new BroadcastSection(false, 0, 0, 5, List.of(), List.of()));
        }

        final boolean waitForAuthMeLogin = fileConfiguration.getBoolean("broadcast.settings.wait_for_authme_login", false);
        final int headerMaxWidth = fileConfiguration.getInt("broadcast.settings.header_max_width", 50);
        final boolean dividerMaxWidth = fileConfiguration.getBoolean("broadcast.settings.divider_max_width", true);
        final String dividerColor = fileConfiguration.getString("broadcast.settings.divider_color", "blue");
        final BroadcastSection join = BroadcastSection.fromSection(fileConfiguration.getConfigurationSection("broadcast.broadcasts.join"));
        final BroadcastSection once = BroadcastSection.fromSection(fileConfiguration.getConfigurationSection("broadcast.broadcasts.once"));
        final BroadcastSection periodic = BroadcastSection.fromSection(fileConfiguration.getConfigurationSection("broadcast.broadcasts.periodic"));

        return new BroadcastConfig(waitForAuthMeLogin, headerMaxWidth, dividerMaxWidth, dividerColor, join, once, periodic);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class BroadcastSection {
        private boolean enabled;
        private int delaySeconds;
        private int intervalMinutes;
        private int delayBetweenSeconds;
        private List<String> targetPlayers;
        private List<BroadcastMessage> messages;

        public static BroadcastSection fromSection(final ConfigurationSection section) {
            if (section == null) {
                return new BroadcastSection(false, 0, 0, 5, List.of(), List.of());
            }

            final boolean enabled = section.getBoolean("enabled", false);
            final int delaySeconds = section.getInt("delay_seconds", 0);
            final int intervalMinutes = section.getInt("interval_minutes", 0);
            final int delayBetweenSeconds = section.getInt("delay_between_seconds", 5);
            final List<String> targetPlayers = section.getStringList("target_players");

            return new BroadcastSection(enabled, delaySeconds, intervalMinutes, delayBetweenSeconds,
                    targetPlayers == null ? List.of() : targetPlayers,
                    readMessages(section));
        }

        private static List<BroadcastMessage> readMessages(final ConfigurationSection section) {
            final List<BroadcastMessage> messages = new ArrayList<>();

            if (section.isList("messages")) {
                final List<Map<?, ?>> rawList = section.getMapList("messages");
                for (final Map<?, ?> rawMessage : rawList) {
                    final String title = rawMessage.get("title") instanceof String ? (String) rawMessage.get("title") : "";
                    final Object linesObject = rawMessage.get("lines");
                    final List<String> lines = linesObject instanceof List ? castStringList((List<?>) linesObject) : Collections.emptyList();
                    final Object targetsObject = rawMessage.get("target_players");
                    final List<String> targetPlayers = targetsObject instanceof List ? castStringList((List<?>) targetsObject) : Collections.emptyList();
                    messages.add(new BroadcastMessage(title, lines, targetPlayers));
                }
                return messages;
            }

            final String title = section.getString("title", "");
            final List<String> lines = section.getStringList("lines");
            if ((title != null && !title.isEmpty()) || (lines != null && !lines.isEmpty())) {
                messages.add(new BroadcastMessage(title == null ? "" : title,
                        lines == null ? Collections.emptyList() : lines,
                        Collections.emptyList()));
            }

            return messages;
        }

        private static List<String> castStringList(final List<?> input) {
            final List<String> output = new ArrayList<>();
            for (final Object value : input) {
                if (value instanceof String) {
                    output.add((String) value);
                }
            }
            return output;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class BroadcastMessage {
        private String title;
        private List<String> lines;
        private List<String> targetPlayers;
    }
}
