package dev.codewithmike.wings.wing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.EntityToggleSwimEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class WingsListener(
    private val wingsManager: WingsManager,
    private val scope: CoroutineScope,
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        scope.launch {
            if (!wingsManager.doesPlayerHaveWings(event.player)) return@launch
            wingsManager.spawnWings(event.player)
        }
    }

    @EventHandler
    fun onPlayerSwimEvent(event: PlayerRespawnEvent) {
        scope.launch {
            if (!wingsManager.doesPlayerHaveWings(event.player)) return@launch
            wingsManager.spawnWings(event.player)
        }
    }

    @EventHandler
    fun onPlayerSwimEvent(event: EntityToggleSwimEvent) {
        val player = event.entity as? Player ?: return
        if (player.isSwimming) {
            scope.launch {
                wingsManager.despawnWingsByPlayer(player)
            }
        } else {
            scope.launch {
                if (!wingsManager.doesPlayerHaveWings(player)) return@launch
                wingsManager.spawnWings(player)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        scope.launch {
            wingsManager.despawnWingsByPlayer(event.player)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        scope.launch {
            wingsManager.despawnWingsByPlayer(event.player)
        }
    }

    @EventHandler
    fun onPlayerGliding(event: EntityToggleGlideEvent) {
        val player = event.entity as? Player ?: return
        scope.launch {
            if (!event.isGliding) {
                wingsManager.spawnWings(player)
            } else {
                wingsManager.despawnWingsByPlayer(player)
            }
        }
    }
}
