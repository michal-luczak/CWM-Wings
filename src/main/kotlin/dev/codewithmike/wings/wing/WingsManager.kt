package dev.codewithmike.wings.wing

import dev.codewithmike.wings.data.definition.WingsDefinitionDto
import dev.codewithmike.wings.data.definition.WingsDefinitionRepository
import dev.codewithmike.wings.data.player.PlayerWingsRepository
import dev.codewithmike.wings.utils.dispatcher.BukkitDispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WingsManager(
    private val wingsSpawner: WingsSpawner
) {

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
                    despawnWingsByPlayerUuid(uuid)
                    spawnWings(player, wingsDefinition.itemModel, wingsDefinition.wingsDefinitionId)
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
                offlinePlayer.player?.let { _ ->
                    despawnWingsByPlayerUuid(playerUuid)
                }
            }
            WingsAdminCommand.WingsResult.success()
        } catch (ex: Exception) {
            WingsAdminCommand.WingsResult.failure("Internal error: ${ex.message ?: "unknown"}")
        }
    }

    fun despawnAllWings() {
        wingEntities.forEach { (_, wings) ->
            wings.armorStand.remove()
        }
        wingEntities.clear()
    }

    suspend fun despawnWingsByPlayerUuid(uuid: UUID) {
        withContext(BukkitDispatchers.main) {
            wingEntities.containsKey(uuid).let {
                wingEntities[uuid]?.armorStand?.remove()
                wingEntities.remove(uuid)
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
    }

    suspend fun turnOffWings(playerUuid: UUID) {
        withContext(BukkitDispatchers.async) {
            PlayerWingsRepository.get(playerUuid)?.let {
                PlayerWingsRepository.turnOffWings(playerUuid)
            }
        }
    }

    private fun despawnWingsByDefinition(wingsName: String) {
        // TODO optimize
        wingEntities.forEach { (playerUuid, wings) ->
            if (wings.wingsDefinitionId == wingsName) {
                wings.armorStand.remove()
                wingEntities.remove(playerUuid)
            }
        }
    }

    private fun spawnWings(player: Player, itemModel: String, wingsDefinitionId: String) {
        val uuid = player.uniqueId
        val armorStand = wingsSpawner.spawnWings(player, itemModel)
        wingEntities[uuid]?.armorStand?.remove()
        wingEntities[uuid] = Wings(armorStand, itemModel, wingsDefinitionId)
    }

    private data class Wings(val armorStand: ArmorStand, val itemModel: String, val wingsDefinitionId: String)
}
