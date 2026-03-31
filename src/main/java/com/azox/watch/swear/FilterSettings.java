package com.azox.watch.swear;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterSettings {

    private boolean logChat;
    private String blockMessage;
    private List<String> blockedWords;

    public static FilterSettings fromConfig(final FileConfiguration config) {
        if (config == null) {
            return new FilterSettings(true, defaultBlockMessage(), Collections.emptyList());
        }

        final boolean logChat = config.getBoolean("swear-filter.log-chat", true);
        final String blockMessage = config.getString("swear-filter.block-message");
        final List<String> blocked = config.getStringList("swear-filter.blocked-words");

        return new FilterSettings(
                logChat,
                blockMessage == null || blockMessage.isBlank() ? defaultBlockMessage() : blockMessage,
                blocked == null ? Collections.emptyList() : blocked
        );
    }

    private static String defaultBlockMessage() {
        return "[Azo] Stopped $playername from saying a bad word.";
    }
}
