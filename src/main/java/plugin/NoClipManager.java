// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

// Owns the set of players in NoClip mode. Keyed by UUID so a username change
// cannot orphan or hijack an entry. Main-thread only, like all Bukkit state.
// Depending plugins reach this through NoClip#getManager().
public class NoClipManager {

    // Players currently in NoClip mode.
    private final Set<UUID> active = new HashSet<>();
    // Saved with the player, independently of the live phasing session. Spawn-OG
    // decides when it may be restored; loading player data never enables /nc.
    private final NamespacedKey intentKey;

    public NoClipManager(NamespacedKey intentKey) {

        this.intentKey = intentKey;

    }

    public boolean hasNoClipIntent(Player player) {

        return player != null && Byte.valueOf((byte) 1)
                .equals(player.getPersistentDataContainer().get(intentKey, PersistentDataType.BYTE));

    }

    // Reports whether the player is currently in NoClip mode.
    public boolean isNoClipping(UUID playerId) {

        return playerId != null && active.contains(playerId);

    }

    // The caller must put the player in creative before entry. Active phasing
    // may re-enter from spectator. Requires noclip.use in either case.
    public boolean enterNoClip(Player player) {

        // Guard against offline handles passed in by other plugins.
        if (player == null || !player.isOnline()) {

            return false;

        }

        // Enter through creative (/gmic) first. Plain spectator must not bypass
        // creative's inventory and region rules; active phasing is idempotent.
        if (player.getGameMode() != GameMode.CREATIVE
                && !(player.getGameMode() == GameMode.SPECTATOR && isNoClipping(player.getUniqueId())))
        {

            return false;

        }

        // Make sure the player has permission first.
        if (!player.hasPermission("noclip.use")) {

            return false;

        }

        // Keeps track of all players in NoClip mode. Re-entry is idempotent.
        active.add(player.getUniqueId());
        player.getPersistentDataContainer().set(intentKey, PersistentDataType.BYTE, (byte) 1);
        return true;

    }

    // Removes the player from NoClip mode without touching gamemode or
    // location. Returns false when the player was not in NoClip mode.
    public boolean exitNoClip(Player player) {

        if (player == null) {

            return false;

        }

        player.getPersistentDataContainer().remove(intentKey);
        return suspendNoClip(player);

    }

    // Disconnect/shutdown stops phasing without forgetting the toggle. Callers
    // decide about gamemode and teleports, as with an explicit exit.
    public boolean suspendNoClip(Player player) {

        if (player == null)
            return false;

        return active.remove(player.getUniqueId());

    }

    // Snapshot copy for shutdown cleanup, so callers can mutate while iterating.
    public Set<UUID> activePlayers() {

        return Set.copyOf(active);

    }

}
