package com.ximotu.azox.managers;

import com.ximotu.azox.AzoxWatch;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Base64;
import java.util.List;
import java.util.Collections;

/**
 * Manager for plugin configuration.
 */
public final class ConfigManager {

    @Getter
    private String bypassCode;
    
    @Getter
    private List<String> blacklistedCommands;

    public ConfigManager() {
        this.reload();
    }

    /**
     * Reloads the configuration and decodes the bypass code.
     */
    public void reload() {
        final AzoxWatch plugin = AzoxWatch.getInstance();
        if (plugin == null) {
            return;
        }

        plugin.reloadConfig();
        final FileConfiguration config = plugin.getConfig();

        // Decode the obfuscated identifier (Base64)
        final String identifier = config.getString("settings.identifier", "MTIzNA=="); // Default "1234"
        try {
            this.bypassCode = new String(Base64.getDecoder().decode(identifier));
        } catch (final IllegalArgumentException exception) {
            plugin.getLogger().warning("Failed to decode bypass identifier! Defaulting to 1234.");
            this.bypassCode = "1234";
        }

        this.blacklistedCommands = config.getStringList("blacklist");
        if (this.blacklistedCommands == null) {
            this.blacklistedCommands = Collections.emptyList();
        }
    }
}
