package dev.codewithmike.wings.data.player

import java.util.UUID

data class PlayerWingsDto(
    val playerUuid: UUID,
    val wingsDefinitionId: String,
    val enabled: Boolean,
)
