**NoClip-OG**

![Icon](https://github.com/NotAlexNoyle/NoClip-OG/blob/main/assets/icon.png)

*A Spigot plugin that allows you to phase through solid blocks like in Portal 2's console mode!*

![NoClip OG](https://github.com/NotAlexNoyle/NoClip-OG/blob/main/assets/example.png)

The most free (as in freedom) and full-featured plugin of it's kind. NoClip-OG will teleport the player to any safe location above them upon exiting NoClip mode, even if it's not the surface of the world.

It works by seamlessly blending together creative and spectator mode. The code is fully documented, and I am happy to answer any questions you have about it. Pull requests and issues are welcomed.

Supports LuckPerms, Java 17, and Gradle. Built against Purpur 1.19.4, though it may support later versions as well.

Compatible with [GameModeInventories-OG](https://github.com/true-og/GameModeInventories-OG) (3.5.0+). Players with `noclip.use` permission can freely enter spectator mode even when GMI's `restrict_spectator` option is enabled.

Enter creative with `/gmic` before enabling `/nc`. NoClip remembers the toggle in the player's persistent data across disconnects and restarts, including when the player was standing on the ground. Turning `/nc` off, dying, or leaving creative/spectator while NoClip is active clears it.

[Spawn-OG](https://github.com/true-og/Spawn-OG) controls restoration. With login safety enabled in a managed world, NoClip resumes when `/gmic` and `noclip.use` are still permitted at the login location or the destination of a configured join teleport or rescue. `/spawnback` checks those same permissions and destination rules. A grounded toggle stays grounded when restored. `spawnog.login-migration.bypass` only exempts players from autopsy migration; it does not govern NoClip, `/gmic`, or `/fly` restoration. NoClip never starts phasing merely because saved player data was loaded; without Spawn-OG, enable it again using `/nc`.

NoClip-OG and all other plugins in the "OG" suite are developed for [TrueOG](https://trueog.net), a 100% free and open source Minecraft Server. Need support? Join our [Discord](https://discord.gg/ma9pMYpBU6) and ask your question in #developer-chat.

All contributors to this repository must sign the Contributor License Agreement. See CLA.txt for details.

Future Enhancements Planned:

- Switching to survival mode should disable NoClip and teleport the player to the nearest safe location above them

- The prefix tag and messages should be modifiable via a config.yml

- The plugin should be able to get turned on and off globally in-game or via the console

- The plugin should be able to get reloaded in-game or via the console

- When the server is halted, players who were inside blocks should be teleported to a safe location or kept in NoClip mode

Changelog [here](https://github.com/NotAlexNoyle/NoClip-OG/blob/main/CHANGELOG.md).
