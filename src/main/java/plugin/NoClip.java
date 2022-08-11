// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle (admin@true-og.net)

package plugin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

// Extending this class is standard bukkit boilerplate for any plugin, or the server software won't load the classes.
public class NoClip extends JavaPlugin {


	// Keep plugin class private so nothing else can use it.
	private static NoClip instance;

	// Getter for instance of primary class. Each new instance in memory is a new player.
	public NoClip() {
	  
		instance = this;
    
	}

	// Public getter for plugin execution
	public static NoClip getPlugin() {
	  
		return instance;
    
	}
 
	// What to do when the plugin is run by the server.
	public void onEnable() {

		// Register the instantiation of the plugin (triggers loading with messages in console).
		Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), (Plugin) this);
		// Run command when plugin event is triggered.
		getCommand("noclip").setExecutor(new CommandManager());
    
	}
	
	// What to do when the plugin is disabled by the server (such as during a reboot).
	public void onDisable() {
		
		// Loop runs once for every player in NoClip mode
		for (String userName : Listeners.getInstance().noclip) {

			// Get player out of block-phase (spectator) mode
			Bukkit.getPlayer(userName).setGameMode(GameMode.CREATIVE);
		    // Remove player from active list
		    Listeners.getInstance().noclip.remove(userName);

		} 
	}
  
}