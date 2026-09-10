package plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoClipManagerTest {

    private final NamespacedKey key = NamespacedKey.fromString("noclip-og:enabled");
    private final Player player = mock(Player.class);
    private final NoClipManager manager = new NoClipManager(key);
    private final AtomicReference<Byte> saved = new AtomicReference<>();

    @BeforeEach
    void setUp() {

        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.isOnline()).thenReturn(true);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        when(player.hasPermission("noclip.use")).thenReturn(true);
        final PersistentDataContainer data = mock(PersistentDataContainer.class);
        when(player.getPersistentDataContainer()).thenReturn(data);
        when(data.get(key, PersistentDataType.BYTE)).thenAnswer(invocation -> saved.get());
        doAnswer(invocation -> {

            saved.set(invocation.getArgument(2));
            return null;

        }).when(data).set(eq(key), eq(PersistentDataType.BYTE), anyByte());
        doAnswer(invocation -> {

            saved.set(null);
            return null;

        }).when(data).remove(key);

    }

    @Test
    void quitAndReloadRememberToggleWithoutStartingPhasing() {

        assertTrue(manager.enterNoClip(player));
        final PlayerQuitEvent quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(player);
        new Listeners(manager).onPlayerQuit(quit);
        assertFalse(manager.isNoClipping(player.getUniqueId()));
        assertTrue(manager.hasNoClipIntent(player));

        final NoClipManager reloaded = new NoClipManager(key);
        assertTrue(reloaded.hasNoClipIntent(player));
        assertFalse(reloaded.isNoClipping(player.getUniqueId()));
        assertTrue(reloaded.enterNoClip(player));
        assertTrue(reloaded.isNoClipping(player.getUniqueId()));

    }

    @Test
    void explicitExitForgetsToggleEvenWhenSuspended() {

        manager.enterNoClip(player);
        manager.suspendNoClip(player);
        manager.exitNoClip(player);
        assertFalse(new NoClipManager(key).hasNoClipIntent(player));

    }

    @Test
    void deathForgetsToggle() {

        manager.enterNoClip(player);
        final PlayerDeathEvent death = mock(PlayerDeathEvent.class);
        when(death.getEntity()).thenReturn(player);
        new Listeners(manager).onPlayerDeath(death);
        assertFalse(manager.isNoClipping(player.getUniqueId()));
        assertFalse(manager.hasNoClipIntent(player));

    }

    @Test
    void creativeSpectatorPhasingRetainsToggleButGmicOffClearsIt() {

        manager.enterNoClip(player);
        final PlayerGameModeChangeEvent change = mock(PlayerGameModeChangeEvent.class);
        when(change.getPlayer()).thenReturn(player);
        when(change.getNewGameMode()).thenReturn(GameMode.SPECTATOR);
        final Listeners listeners = new Listeners(manager);
        listeners.onGameModeChange(change);
        assertTrue(manager.isNoClipping(player.getUniqueId()));
        assertTrue(manager.hasNoClipIntent(player));
        when(change.getNewGameMode()).thenReturn(GameMode.SURVIVAL);
        listeners.onGameModeChange(change);
        assertFalse(manager.isNoClipping(player.getUniqueId()));
        assertFalse(manager.hasNoClipIntent(player));

    }

    @Test
    void loginNormalizationDoesNotEraseSuspendedIntent() {

        manager.enterNoClip(player);
        manager.suspendNoClip(player);
        final PlayerGameModeChangeEvent change = mock(PlayerGameModeChangeEvent.class);
        when(change.getPlayer()).thenReturn(player);
        when(change.getNewGameMode()).thenReturn(GameMode.SURVIVAL);
        new Listeners(manager).onGameModeChange(change);
        assertTrue(manager.hasNoClipIntent(player));

    }

    @Test
    void entryRequiresCreativeAndPermissionButActivePhasingIsIdempotent() {

        when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        assertFalse(manager.enterNoClip(player));
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        assertFalse(manager.enterNoClip(player));
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        when(player.hasPermission("noclip.use")).thenReturn(false);
        assertFalse(manager.enterNoClip(player));
        assertFalse(manager.hasNoClipIntent(player));
        when(player.hasPermission("noclip.use")).thenReturn(true);
        assertTrue(manager.enterNoClip(player));
        when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);
        assertTrue(manager.enterNoClip(player));
        when(player.isOnline()).thenReturn(false);
        assertFalse(manager.enterNoClip(player));

    }

}
