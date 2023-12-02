// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

// Extends bukkit class to run commands.
public class CommandManager implements CommandExecutor {

	// Keep inheritance of command manager private so nothing else can hook into it and run an unrelated command.
	private static CommandManager instance;

	// Static getter allows multiple players to be processed through the same class declaration.
	public static CommandManager getInstance() {

		return instance;

	}

	// Command execution event handler extending bukkit's CommandManager
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		// Create a colored Prefix using the Minimessage API.
		String chatPrefix = "&8[&aNoClip &4OG&6&8] ";		
		TextComponent chatPrefixContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(chatPrefix);

		// Takes over command execution if plugin is invoked.
		if (cmd.getName().equalsIgnoreCase("noclip")) {

			// Checks to make sure the command is being run in-game and not in the console.
			if (sender instanceof Player) {

				// Convert sender to player once it has been determined that they are one.
				Player player = (Player) sender;

				// Only run the command in appropriate gamemodes.
				if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {

					// Make sure the player has permission first.
					if (player.hasPermission("noclip.use")) {

						// TODO: Update gradle
						// TODO: Add prefix to messages
						// Checks whether the "enabled" code has been run for the player yet.
						if (Listeners.getInstance().noclip.contains(player.getName())) {

							// Remove player from NoClip mode
							Listeners.getInstance().noclip.remove(player.getName());
							// Teleport player to nearest safe location above their head.
							teleportToSafety(player, chatPrefixContainer);
							// Return player to creative mode so they no longer phase through blocks.
							player.setGameMode(GameMode.CREATIVE);

							// Create a colored disabled message using the TextComponent API.
							String disabledMessage = chatPrefix + "&6NoClip mode disabled!";		
							TextComponent disabledMessageContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(chatPrefixdisabledMessage);

							// Confirm that the plugin has been shut off to the user.
							player.sendMessage(disabledMessageContainer);

						}
						// 
						else {

							// Keeps track of all players in NoClip mode.
							Listeners.getInstance().noclip.add(player.getName());

							// Create a colored enabled message using the TextComponent API.
							String enabledMessage = chatPrefix + "&aNoClip mode enabled!";		
							TextComponent enabledMessageContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(enabledMessage);

							// Confirm that the plugin has been turned on to the user.
							player.sendMessage(enabledMessageContainer);

						}

					}

				}
				// Do nothing if run from survival mode.
				else {

					// Create a colored gamemode error message using the TextComponent API.
					String gamemodeErrorMessage = chatPrefix + "&cERROR: You must be in creative to use this command!";		
					TextComponent gamemodeErrorContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(gamemodeErrorMessage);

					// Send the error message to the player.
					player.sendMessage(gamemodeErrorContainer);

				}

			}
			// Do nothing if run from console.
			else {

				// Send error message to console.
				sender.sendMessage("[NoClip-OG] ERROR: The console cannot execute this command!");

			}

		}

		// Healthy exit status.
		return true;

	}

	// Teleports player to nearest safe (creative) location.
	// Don't use this for survival, it has no mitigation for lava or other threats.
	public void teleportToSafety(Player player, TextComponent chatPrefix) {

		// Fetch player location.
		Location newPlayerLocation = player.getLocation();
		// Assume location is unsafe before testing it.
		boolean blockUnsafe = true;
		while(blockUnsafe) {

			// If location is not air...
			if(locationIsSolid(newPlayerLocation, player)) {

				// Move location to check one block up.
				newPlayerLocation = newPlayerLocation.add(0, 1, 0);

			}
			// Runs if new foot location is safe.
			else {

				// Runs if new head location is unsafe.
				if(locationIsSolid(newPlayerLocation.add(0, 1, 0), player)) {

					// Keep scanning for a safe location higher up.
					newPlayerLocation = newPlayerLocation.add(0, 1, 0);

				}
				// Runs if both new head and foot location are safe.
				else {

					// Makes this the last iteration of the loop.
					blockUnsafe = false;

					// Teleports the player to a new, safe location above them.
					player.teleport(newPlayerLocation);

					// Create a colored disabled message using the TextComponent API.
					String nearesetSafeLocationMessage = chatPrefix + "&aYou have been teleported to the nearest safe location above you.";		
					TextComponent nearesetSafeLocationContainer = LegacyComponentSerializer.legacyAmpersand().deserialize(nearesetSafeLocationMessage);

					// Inform the player that their return teleport location has changed.
					player.sendMessage(nearesetSafeLocationContainer);

				}

			}

		}

	}


	// Checks whether block will make player stuck or not.
	public boolean locationIsSolid(Location location, Player player) {

		// If block is unsafe.
		if(location.getBlock().getType().isSolid()) {

			return true;

		}
		// If block is safe.
		else {

			return false;

		}

	}

}