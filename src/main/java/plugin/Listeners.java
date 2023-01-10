// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Listeners implements Listener {

	// Declare instance of class as static so multiple players can use it.
	private static Listeners instance;
	// Declare object to hold players in NoClip mode in memory
	public ArrayList<String> noclip = new ArrayList<>();;
	// Declare bukkit object to determine whether a player is intentionally trying to phase into a block or not
	private final BlockFace[] surrounding;

	// Allow phasing into a block from any direction
	public Listeners() {

		this.surrounding = new BlockFace[] { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
		instance = this;

	}

	// Return instance of class as static so multiple players can use it.
	public static Listeners getInstance() {

		return instance;

	}

	// Hooks player movement event
	@EventHandler
	public void onNearBlock(PlayerMoveEvent playerMovement) {

		// Derive the relevant player object from their movement event
		Player player = playerMovement.getPlayer();

		// If player is not in survival AND they have a registered NoClip instance in the array list
		if ((player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) && this.noclip.contains(player.getName())) {

			// If player is trying to phase into a block, do this
			if (nearBlock(player)) {

				// Use spectator mode to enable phasing
				player.setGameMode(GameMode.SPECTATOR);

			}
			else {

				// User creative mode to disable phasing
				player.setGameMode(GameMode.CREATIVE);

			}  

		}

	}

	// Hook player death listener
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent playerThatDied) {

		// Derive player object from their death event
		Player player = playerThatDied.getEntity();

		// If player has a NoClip instance in the array list...
		if (Listeners.getInstance().noclip.contains(player.getName())) {

			// Remove player from NoClip mode
			Listeners.getInstance().noclip.remove(player.getName());
			// Set gamemode back to survival upon death
			player.setGameMode(GameMode.SURVIVAL);

		} 

	}

	// Determine if a player is trying to phase into their environment
	public boolean nearBlock(Player player) {

		// Declare array to hold block locations at and near the player
		Location[] nearby = {
				player.getLocation(), 
				player.getLocation().add(0.0D, 1.0D, 0.0D), 
				player.getLocation().add(0.0D, 2.0D, 0.0D) 
		};

		// First loop runs as many times as there are locations near and at the player which we have declared in the nearby array
		for (int i = 0; i < nearby.length; i++) {

			// Feed surrounding block faces into an array for evaluation
			BlockFace[] arrayOfBlockFace = this.surrounding;
			// Second loop evaluates all relevant block faces at current frame of reference
			for (int j = 0; j < arrayOfBlockFace.length; j++) {

				// Isolates the block face to phase into
				BlockFace staringAt = arrayOfBlockFace[j];

				// If the block is solid, initiate the phase
				if (! nearby[i].getBlock().getRelative(staringAt, 1).isEmpty()) {

					// Player is near block and wants to phase into it
					return true;

				}

			}

		} 

		// If nearby blocks are not empty, player is near a block
		if (! player.getLocation().add(0.0D, 2.0D, 0.0D).getBlock().isEmpty() || ! player.getLocation().subtract(0.0D, 1.0D, 0.0D).getBlock().isEmpty()) {

			return true; 

		}

		// None of the conditions have been met for phasing
		return false;

	}

}