// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

// Extending this class is standard bukkit boilerplate for any plugin, or else the server software won't load the
// classes.
public class NoClip extends JavaPlugin {

    // Shared NoClip state, injected into the listener and the command handler.
    private NoClipManager manager;

    // What to do when the plugin is run by the server.
    public void onEnable() {

        // Create the shared state holder before anything can consume it.
        manager = new NoClipManager();
        // Register the instantiation of the plugin (triggers loading with messages in
        // console).
        getServer().getPluginManager().registerEvents(new Listeners(manager), (Plugin) this);
        // Run command when plugin event is triggered.
        getCommand("noclip").setExecutor(new CommandManager(manager));

    }

    // Compile-time API for depending plugins (such as Spawn-OG) to query and
    // drive NoClip state on their own players.
    public NoClipManager getManager() {

        return manager;

    }

    // What to do when the plugin is disabled by the server (such as during a
    // reboot).
    public void onDisable() {

        // Iterate a snapshot copy so removal cannot upset the loop.
        for (UUID playerId : manager.activePlayers()) {

            // The player may already be gone; only touch the ones still online.
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {

                // Get player out of block-phase (spectator) mode.
                player.setGameMode(GameMode.CREATIVE);

            }

            // Remove player from active list.
            if (player != null) {

                manager.exitNoClip(player);

            }

        }

    }

}
