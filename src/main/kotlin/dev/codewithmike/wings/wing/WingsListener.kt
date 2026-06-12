package dev.codewithmike.wings.wing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleSwimEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class WingsListener(
    private val wingsManager: WingsManager,
    private val scope: CoroutineScope
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
        if (event.entity !is Player) return
        if (event.isSwimming) {
            scope.launch {
                wingsManager.despawnWingsByPlayerUuid(event.entity.uniqueId)
            }
        } else {
            scope.launch {
                if (!wingsManager.doesPlayerHaveWings(event.entity as Player)) return@launch
                wingsManager.spawnWings(event.entity as Player)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        scope.launch {
            wingsManager.despawnWingsByPlayerUuid(event.player.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        scope.launch {
            wingsManager.despawnWingsByPlayerUuid(event.player.uniqueId)
        }
    }
}