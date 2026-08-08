// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

// Owns the set of players in NoClip mode. Keyed by UUID so a username change
// cannot orphan or hijack an entry. Main-thread only, like all Bukkit state.
// Depending plugins reach this through NoClip#getManager().
public class NoClipManager {

    // Players currently in NoClip mode.
    private final Set<UUID> active = new HashSet<>();

    // Reports whether the player is currently in NoClip mode.
    public boolean isNoClipping(UUID playerId) {

        return playerId != null && active.contains(playerId);

    }

    // Puts the player into NoClip mode. The caller is responsible for the
    // gamemode: the player must already be in creative or spectator, and must
    // hold noclip.use. Returns false when either precondition fails.
    public boolean enterNoClip(Player player) {

        // Guard against offline handles passed in by other plugins.
        if (player == null || !player.isOnline()) {

            return false;

        }

        // NoClip phases between creative and spectator, so it can only engage
        // from one of those modes.
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {

            return false;

        }

        // Make sure the player has permission first.
        if (!player.hasPermission("noclip.use")) {

            return false;

        }

        // Keeps track of all players in NoClip mode. Re-entry is idempotent.
        active.add(player.getUniqueId());
        return true;

    }

    // Removes the player from NoClip mode without touching gamemode or
    // location. Returns false when the player was not in NoClip mode.
    public boolean exitNoClip(Player player) {

        if (player == null) {

            return false;

        }

        // State removal only; callers decide about gamemode and teleports.
        return active.remove(player.getUniqueId());

    }

    // Snapshot copy for shutdown cleanup, so callers can mutate while iterating.
    public Set<UUID> activePlayers() {

        return Set.copyOf(active);

    }

}
