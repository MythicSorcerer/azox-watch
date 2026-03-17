package com.ximotu.azox.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manager for players who have bypassed command restrictions.
 */
public final class BypassManager {

    private final Set<UUID> bypassedPlayers = new HashSet<>();

    /**
     * Grants bypass status to a player.
     *
     * @param uuid The player's UUID.
     */
    public void add(final UUID uuid) {
        this.bypassedPlayers.add(uuid);
    }

    /**
     * Removes bypass status from a player.
     *
     * @param uuid The player's UUID.
     */
    public void remove(final UUID uuid) {
        this.bypassedPlayers.remove(uuid);
    }

    /**
     * Checks if a player has bypass status.
     *
     * @param uuid The player's UUID.
     * @return True if bypassed.
     */
    public boolean isBypassed(final UUID uuid) {
        return this.bypassedPlayers.contains(uuid);
    }
}
