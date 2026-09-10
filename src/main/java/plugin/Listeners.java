// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {

    // Shared NoClip state, owned by the plugin and injected here.
    private final NoClipManager manager;
    // Declare bukkit object to determine whether a player is intentionally trying
    // to phase into a block or not
    private final BlockFace[] surrounding;

    // Allow phasing into a block from any direction
    public Listeners(NoClipManager manager) {

        this.manager = manager;
        this.surrounding = new BlockFace[] { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST,
                BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    }

    // Hooks player movement event
    @EventHandler
    public void onNearBlock(PlayerMoveEvent playerMovement) {

        // Derive the relevant player object from their movement event
        final Player player = playerMovement.getPlayer();

        // If player is not in survival AND they have a registered NoClip instance
        if ((player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
                && manager.isNoClipping(player.getUniqueId()))
        {

            // If player is trying to phase into a block, do this
            if (nearBlock(player)) {

                // Use spectator mode to enable phasing (skip if already set to avoid
                // firing PlayerGameModeChangeEvent every tick, which can spam listeners
                // on other plugins that restrict spectator mode)
                if (player.getGameMode() != GameMode.SPECTATOR) {

                    player.setGameMode(GameMode.SPECTATOR);

                }

            } else {

                // Use creative mode to disable phasing (skip if already set)
                if (player.getGameMode() != GameMode.CREATIVE) {

                    player.setGameMode(GameMode.CREATIVE);

                }

            }

        }

    }

    // Hook player death listener
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerThatDied) {

        // Derive player object from their death event
        final Player player = playerThatDied.getEntity();

        // If player has a NoClip instance in the set...
        if (manager.exitNoClip(player)) {

            // Set gamemode back to survival upon death
            player.setGameMode(GameMode.SURVIVAL);

        }

    }

    // /gmic off and region-enforced survival end the active toggle. Cancelled
    // changes and creative/spectator phasing leave it intact. An inactive login
    // may be normalized before Spawn-OG reads the saved toggle, so leave it alone.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {

        if (event.getNewGameMode() != GameMode.CREATIVE && event.getNewGameMode() != GameMode.SPECTATOR
                && manager.isNoClipping(event.getPlayer().getUniqueId()))
            manager.exitNoClip(event.getPlayer());

    }

    // MONITOR so Spawn-OG's LOWEST-priority quit snapshot reads NoClip state
    // before it is cleared here. Kicks funnel into this same quit event; a
    // dedicated kick handler would run before that snapshot and corrupt it.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent playerThatQuit) {

        // Derive player object from their quit event
        final Player player = playerThatQuit.getPlayer();

        // If player has a NoClip instance in the set...
        if (manager.suspendNoClip(player)) {

            // Leave creative behind, never spectator, so a relog on a server
            // without Spawn-OG does not strand the player phased into a block.
            if (player.getGameMode() != GameMode.CREATIVE) {

                player.setGameMode(GameMode.CREATIVE);

            }

        }

    }

    // Determine if a player is trying to phase into their environment
    public boolean nearBlock(Player player) {

        // Declare array to hold block locations at and near the player
        final Location[] nearby = { player.getLocation(), player.getLocation().add(0.0D, 1.0D, 0.0D),
                player.getLocation().add(0.0D, 2.0D, 0.0D) };

        // First loop runs as many times as there are locations near and at the player
        // which we have declared in the
        // nearby array
        for (int i = 0; i < nearby.length; i++) {

            // Feed surrounding block faces into an array for evaluation
            final BlockFace[] arrayOfBlockFace = this.surrounding;
            // Second loop evaluates all relevant block faces at current frame of reference
            for (int j = 0; j < arrayOfBlockFace.length; j++) {

                // Isolates the block face to phase into
                final BlockFace staringAt = arrayOfBlockFace[j];

                // If the block is solid, initiate the phase
                if (!nearby[i].getBlock().getRelative(staringAt, 1).isEmpty()) {

                    // Player is near block and wants to phase into it
                    return true;

                }

            }

        }

        // If nearby blocks are not empty, player is near a block
        if (!player.getLocation().add(0.0D, 2.0D, 0.0D).getBlock().isEmpty()
                || !player.getLocation().subtract(0.0D, 1.0D, 0.0D).getBlock().isEmpty())
        {

            return true;

        }

        // None of the conditions have been met for phasing
        return false;

    }

}
