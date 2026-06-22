package dev.codewithmike.wings.data.player

import org.jetbrains.exposed.v1.core.Table

object PlayerWingsTable : Table("cwm_player_wings") {
    val playerUuid = text("player_uuid")
    val wingsDefinitionId = text("wings_id")
    val enabled = bool("enabled")

    override val primaryKey = PrimaryKey(playerUuid)
}
