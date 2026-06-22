package dev.codewithmike.wings.data.definition

import org.jetbrains.exposed.v1.core.Table

object WingsDefinitionTable : Table("cwm_wings_definition") {
    val wingsDefinitionId = text("wings_id")
    val itemModel = text("item_model")

    override val primaryKey = PrimaryKey(wingsDefinitionId)
}
