package dev.codewithmike.wings.wing

import dev.codewithmike.wings.data.definition.WingsDefinitionDto
import dev.codewithmike.wings.data.definition.WingsDefinitionRepository
import dev.codewithmike.wings.data.player.PlayerWingsRepository
import dev.codewithmike.wings.utils.dispatcher.BukkitDispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_RESONATE
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WingsManager {

    private val wingEntities: MutableMap<UUID, Wings> = ConcurrentHashMap()

    suspend fun doesPlayerHaveWings(player: Player): Boolean = withContext(BukkitDispatchers.async) {
        PlayerWingsRepository.get(player.uniqueId) != null
    }

    suspend fun giveWingsToPlayer(
        wings: String,
        offlinePlayer: OfflinePlayer
    ): WingsAdminCommand.WingsResult {
        val uuid = offlinePlayer.uniqueId
        return try {
            val wingsDefinition: WingsDefinitionDto = withContext(BukkitDispatchers.async) {
                val wingsDefinitionDto = WingsDefinitionRepository.getWingsDefinition(wings)
                    ?: return@withContext null
                PlayerWingsRepository.grant(uuid, wingsDefinitionDto.wingsDefinitionId)
                wingsDefinitionDto
            } ?: return WingsAdminCommand.WingsResult.failure(
                "No such wings $wings"
            )
            withContext(BukkitDispatchers.main) {
                offlinePlayer.player?.let { player ->
                    despawnWingsByPlayer(player)
                    spawnWings(player, wingsDefinition.itemModel, wingsDefinition.wingsDefinitionId)
                    player.playSound(player, BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1f)
                }
            }
            WingsAdminCommand.WingsResult.success()
        } catch (ex: Exception) {
            WingsAdminCommand.WingsResult.failure(
                "Internal error: ${ex.message ?: "unknown"}"
            )
        }
    }

    suspend fun removeWingsFromPlayer(
        offlinePlayer: OfflinePlayer
    ): WingsAdminCommand.WingsResult {
        val playerUuid = offlinePlayer.uniqueId
        withContext(BukkitDispatchers.async) {
            if (PlayerWingsRepository.get(playerUuid) == null) {
                return@withContext WingsAdminCommand.WingsResult(
                    result = WingsAdminCommand.WingsResult.WingsResultType.FAILURE,
                    reason = "Player ${offlinePlayer.name} has no wings"
                )
            }
        }
        return try {
            withContext(BukkitDispatchers.async) {
                PlayerWingsRepository.revoke(playerUuid)
            }
            withContext(BukkitDispatchers.main) {
                offlinePlayer.player?.let { player ->
                    despawnWingsByPlayer(player)
                }
            }
            WingsAdminCommand.WingsResult.success()
        } catch (ex: Exception) {
            WingsAdminCommand.WingsResult.failure("Internal error: ${ex.message ?: "unknown"}")
        }
    }

    fun despawnAllWings() {
        wingEntities.forEach { (_, wings) ->
            wings.despawn()
        }
        wingEntities.clear()
    }

    suspend fun despawnWingsByPlayer(player: Player) {
        val uniqueId = player.uniqueId
        withContext(BukkitDispatchers.main) {
            wingEntities.containsKey(player.uniqueId).let {
                wingEntities[uniqueId]?.despawn()
                wingEntities.remove(uniqueId)
            }
        }
    }

    suspend fun spawnWings(player: Player) {
        val pair = withContext(BukkitDispatchers.async) {
            val playerWings = PlayerWingsRepository.get(player.uniqueId) ?: return@withContext null
            val definition = WingsDefinitionRepository.getWingsDefinition(playerWings.wingsDefinitionId) ?: return@withContext null
            playerWings to definition
        } ?: return

        val (playerWings, wingsDefinition) = pair

        withContext(BukkitDispatchers.main) {
            if (!playerWings.enabled) return@withContext
            spawnWings(player, wingsDefinition.itemModel, wingsDefinition.wingsDefinitionId)
        }
    }

    suspend fun respawnAllWings() {
        val allItemModels = withContext(BukkitDispatchers.async) {
            WingsDefinitionRepository.getAllItemModels()
        }
        withContext(BukkitDispatchers.main) {
            despawnAllWings()
            PlayerWingsRepository.findAll().forEach { playerWingsDto ->
                if (!playerWingsDto.enabled) return@forEach
                Bukkit.getOfflinePlayer(playerWingsDto.playerUuid).player?.let { player ->
                    allItemModels[playerWingsDto.wingsDefinitionId]?.let {
                        spawnWings(player, it, playerWingsDto.wingsDefinitionId)
                    }
                }
            }
        }
    }

    suspend fun createWingsDefinition(wingsName: String, itemModel: String): WingsAdminCommand.WingsResult {
        return withContext(BukkitDispatchers.async) {
            WingsDefinitionRepository.createWingsDefinition(wingsName, itemModel)
            return@withContext WingsAdminCommand.WingsResult.success()
        }
    }

    suspend fun deleteWingsDefinition(wings: String) {
        withContext(BukkitDispatchers.async) {
            WingsDefinitionRepository.deleteByName(wings)
        }
        withContext(BukkitDispatchers.main) {
            despawnWingsByDefinition(wings)
        }
    }

    suspend fun getWingsDefinition(wingsDefinitionId: String): WingsDefinitionDto? = withContext(BukkitDispatchers.async) {
        WingsDefinitionRepository.getWingsDefinition(wingsDefinitionId)
    }

    suspend fun getWingsDefinitions(): Collection<String> = withContext(BukkitDispatchers.async) {
        WingsDefinitionRepository.getAllItemModels().keys
    }

    suspend fun turnOnWings(playerUuid: UUID) {
        withContext(BukkitDispatchers.async) {
            PlayerWingsRepository.get(playerUuid)?.let {
                PlayerWingsRepository.turnOnWings(playerUuid)
            }
        }
        withContext(BukkitDispatchers.main) {
            Bukkit.getOfflinePlayer(playerUuid).player?.let { spawnWings(it) }
        }
    }

    suspend fun turnOffWings(playerUuid: UUID) {
        withContext(BukkitDispatchers.async) {
            PlayerWingsRepository.get(playerUuid)?.let {
                PlayerWingsRepository.turnOffWings(playerUuid)
            }
        }
        withContext(BukkitDispatchers.main) {
            Bukkit.getOfflinePlayer(playerUuid).player?.let { despawnWingsByPlayer(it) }
        }
    }

    private fun despawnWingsByDefinition(wingsName: String) {
        wingEntities.forEach { (playerUuid, wings) ->
            if (wings.wingsDefinitionId == wingsName) {
                wings.despawn()
                wingEntities.remove(playerUuid)
            }
        }
    }

    private fun spawnWings(player: Player, itemModel: String, wingsDefinitionId: String) {
        val uuid = player.uniqueId
        wingEntities[uuid]?.despawn()
        wingEntities[uuid] = Wings(itemModel, wingsDefinitionId, player)
    }
}
