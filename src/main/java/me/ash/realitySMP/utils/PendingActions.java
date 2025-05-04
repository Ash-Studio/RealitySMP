package me.ash.realitySMP.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class to track pending player actions
 */
public class PendingActions {
    private final Map<UUID, String> pendingActions;

    public PendingActions() {
        this.pendingActions = new HashMap<>();
    }

    /**
     * Add a pending action for a player
     *
     * @param uuid Player UUID
     * @param action Action string
     */
    public void put(UUID uuid, String action) {
        pendingActions.put(uuid, action);
    }

    /**
     * Get a player's pending action
     *
     * @param uuid Player UUID
     * @return The pending action string, or null if none exists
     */
    public String get(UUID uuid) {
        return pendingActions.get(uuid);
    }

    /**
     * Remove a player's pending action
     *
     * @param uuid Player UUID
     * @return The removed action, or null if none existed
     */
    public String remove(UUID uuid) {
        return pendingActions.remove(uuid);
    }

    /**
     * Check if a player has a pending action
     *
     * @param uuid Player UUID
     * @return true if the player has a pending action
     */
    public boolean has(UUID uuid) {
        return pendingActions.containsKey(uuid);
    }

    /**
     * Clear all pending actions
     */
    public void clear() {
        pendingActions.clear();
    }
}