package dev.codewithmike.wings.data.player

import dev.codewithmike.wings.data.player.PlayerWingsTable.enabled
import dev.codewithmike.wings.data.player.PlayerWingsTable.playerUuid
import dev.codewithmike.wings.data.player.PlayerWingsTable.wingsDefinitionId
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

object PlayerWingsRepository {
    fun get(playerUuid: UUID): PlayerWingsDto? =
        transaction {
            PlayerWingsTable
                .selectAll()
                .where { PlayerWingsTable.playerUuid eq playerUuid.toString() }
                .map {
                    PlayerWingsDto(
                        playerUuid = UUID.fromString(it[PlayerWingsTable.playerUuid]),
                        wingsDefinitionId = it[wingsDefinitionId],
                        enabled = it[enabled],
                    )
                }.singleOrNull()
        }

    fun findAll(): Set<PlayerWingsDto> =
        transaction {
            PlayerWingsTable
                .selectAll()
                .map {
                    PlayerWingsDto(
                        playerUuid = UUID.fromString(it[playerUuid]),
                        wingsDefinitionId = it[wingsDefinitionId],
                        enabled = it[enabled],
                    )
                }.toSet()
        }

    fun grant(
        playerUuid: UUID,
        wingsDefinitionId: String,
    ) = transaction {
        PlayerWingsTable.upsert {
            it[this.playerUuid] = playerUuid.toString()
            it[PlayerWingsTable.wingsDefinitionId] = wingsDefinitionId
        }
    }

    fun revoke(playerUuid: UUID) =
        transaction {
            PlayerWingsTable.deleteWhere {
                this.playerUuid eq playerUuid.toString()
            }
        }

    fun turnOnWings(playerUuid: UUID) =
        transaction {
            PlayerWingsTable.update(
                { PlayerWingsTable.playerUuid eq playerUuid.toString() },
            ) {
                it[enabled] = true
            }
        }

    fun turnOffWings(playerUuid: UUID) =
        transaction {
            PlayerWingsTable.update(
                { PlayerWingsTable.playerUuid eq playerUuid.toString() },
            ) {
                it[enabled] = false
            }
        }
}
