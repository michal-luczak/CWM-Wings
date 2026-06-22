package dev.codewithmike.wings.wing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.EntityMountEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.EntityToggleSwimEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import kotlin.time.Duration.Companion.milliseconds

class WingsListener(
    private val wingsManager: WingsManager,
    private val scope: CoroutineScope,
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        scope.launch {
            wingsManager.spawnWings(event.player)
        }
    }

    @EventHandler
    fun onPlayerSwimEvent(event: PlayerRespawnEvent) {
        scope.launch {
            wingsManager.spawnWings(event.player)
        }
    }

    @EventHandler
    fun onPlayerSwimEvent(event: EntityToggleSwimEvent) {
        val player = event.entity as? Player ?: return
        scope.launch {
            if (event.isSwimming) {
                wingsManager.despawnWingsByPlayer(player)
            } else {
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

    @EventHandler(ignoreCancelled = true)
    fun onEntityMount(event: EntityMountEvent) {
        val player = event.mount as? Player ?: return
        val passenger = event.entity

        if (passenger.scoreboardTags.contains("cwm_wings")) return

        scope.launch {
            wingsManager.despawnWingsByPlayer(player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDismount(event: EntityDismountEvent) {
        val player = event.dismounted as? Player ?: return
        val passenger = event.entity
        if (passenger.scoreboardTags.contains("cwm_wings")) return
        val realPassengersCount = player.passengers.count { !it.scoreboardTags.contains("cwm_wings") }
        if (realPassengersCount <= 1) {
            scope.launch {
                delay(50.milliseconds)
                wingsManager.spawnWings(player)
            }
        }
    }
}
