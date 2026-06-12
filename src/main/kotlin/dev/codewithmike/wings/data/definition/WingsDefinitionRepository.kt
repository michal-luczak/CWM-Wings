package dev.codewithmike.wings.data.definition

import dev.codewithmike.wings.data.definition.WingsDefinitionTable.itemModel
import dev.codewithmike.wings.data.definition.WingsDefinitionTable.wingsDefinitionId
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

object WingsDefinitionRepository {

    fun getWingsDefinition(wingsId: String): WingsDefinitionDto? = transaction {
        WingsDefinitionTable
            .selectAll()
            .where { wingsDefinitionId eq wingsId }
            .map { WingsDefinitionDto(
                wingsDefinitionId = wingsId,
                itemModel = it[itemModel],
            ) }
            .singleOrNull()
    }

    fun getAllItemModels(): Map<String, String> = transaction {
        WingsDefinitionTable
            .select(listOf(wingsDefinitionId, itemModel))
            .associate {
                it[wingsDefinitionId] to it[itemModel]
            }
    }

    fun createWingsDefinition(wingsId: String, itemModel: String) = transaction {
        WingsDefinitionTable.upsert{
            it[wingsDefinitionId] = wingsId
            it[WingsDefinitionTable.itemModel] = itemModel
        }
    }

    fun deleteByName(wingsName: String) = transaction {
        WingsDefinitionTable.deleteWhere { wingsDefinitionId eq wingsName }
    }
}