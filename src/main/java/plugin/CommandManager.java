// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Extends bukkit class to run commands.
public class CommandManager implements CommandExecutor {

    // Shared NoClip state, owned by the plugin and injected here.
    private final NoClipManager manager;

    public CommandManager(NoClipManager manager) {

        this.manager = manager;

    }

    // Command execution event handler extending bukkit's CommandManager
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Create a colored Prefix using the Minimessage API.
        final String chatPrefix = "&8[&aNoClip &4OG&6&8] ";

        // Takes over command execution if plugin is invoked.
        if (cmd.getName().equalsIgnoreCase("noclip")) {

            // Checks to make sure the command is being run in-game and not in the console.
            if (sender instanceof Player) {

                // Convert sender to player once it has been determined that they are one.
                final Player player = (Player) sender;

                // Only run the command in appropriate gamemodes.
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {

                    // Checks whether the "enabled" code has been run for the player yet.
                    if (manager.isNoClipping(player.getUniqueId())) {

                        // Remove player from NoClip mode
                        manager.exitNoClip(player);
                        // Teleport player to nearest safe location above their head.
                        teleportToSafety(player, chatPrefix);
                        // Return player to creative mode so they no longer phase through blocks.
                        player.setGameMode(GameMode.CREATIVE);

                        // Create a colored disabled message using the TextComponent API.
                        final String disabledMessage = chatPrefix + "&6NoClip mode disabled!";
                        final TextComponent disabledMessageContainer = LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(disabledMessage);

                        // Confirm that the plugin has been shut off to the user.
                        player.sendMessage(disabledMessageContainer);

                    }
                    // Engage NoClip mode, permission permitting.
                    else if (manager.enterNoClip(player)) {

                        // Create a colored enabled message using the TextComponent API.
                        final String enabledMessage = chatPrefix + "&aNoClip mode enabled!";
                        final TextComponent enabledMessageContainer = LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(enabledMessage);

                        // Confirm that the plugin has been turned on to the user.
                        player.sendMessage(enabledMessageContainer);

                    }
                    // The only remaining reason entry can fail here is permission.
                    else {

                        // Create a colored permission error message using the TextComponent API.
                        final String permissionErrorMessage = chatPrefix
                                + "&cERROR: You do not have permission to use this command!";
                        final TextComponent permissionErrorContainer = LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(permissionErrorMessage);

                        // Send the error message to the player.
                        player.sendMessage(permissionErrorContainer);

                    }

                }
                // Do nothing if run from survival mode.
                else {

                    // Create a colored gamemode error message using the TextComponent API.
                    final String gamemodeErrorMessage = chatPrefix
                            + "&cERROR: You must be in creative to use this command!";
                    final TextComponent gamemodeErrorContainer = LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(gamemodeErrorMessage);

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
    public void teleportToSafety(Player player, String chatPrefix) {

        // Fetch player location.
        Location newPlayerLocation = player.getLocation();
        // Assume location is unsafe before testing it.
        boolean blockUnsafe = true;
        while (blockUnsafe) {

            // If location is not air...
            if (locationIsSolid(newPlayerLocation, player)) {

                // Move location to check one block up.
                newPlayerLocation = newPlayerLocation.add(0, 1, 0);

            }
            // Runs if new foot location is safe.
            else {

                // Runs if new head location is unsafe.
                if (locationIsSolid(newPlayerLocation.add(0, 1, 0), player)) {

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
                    final String nearesetSafeLocationMessage = chatPrefix
                            + "&aYou have been teleported to the nearest safe location above you.";
                    final TextComponent nearesetSafeLocationContainer = LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(nearesetSafeLocationMessage);

                    // Inform the player that their return teleport location has changed.
                    player.sendMessage(nearesetSafeLocationContainer);

                }

            }

        }

    }

    // Checks whether block will make player stuck or not.
    public boolean locationIsSolid(Location location, Player player) {

        // If block is unsafe.
        if (location.getBlock().getType().isSolid()) {

            return true;

        }
        // If block is safe.
        else {

            return false;

        }

    }

}
