package dev.codewithmike.wings

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.codewithmike.wings.data.definition.WingsDefinitionTable
import dev.codewithmike.wings.data.player.PlayerWingsTable
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class CascadeDeleteTest {
    private lateinit var dataSource: HikariDataSource

    @BeforeEach
    fun setup() {
        val dbFile = File("test-database.db")
        if (dbFile.exists()) dbFile.delete()

        val dbConfig =
            HikariConfig().apply {
                jdbcUrl = "jdbc:sqlite:test-database.db"
                maximumPoolSize = 1
                connectionInitSql = "PRAGMA foreign_keys = ON"
            }
        dataSource = HikariDataSource(dbConfig)

        Flyway
            .configure()
            .dataSource(dataSource)
            .locations("filesystem:src/main/resources/db/migration")
            .load()
            .migrate()

        Database.connect(dataSource)
    }

    @Test
    fun `test cascade delete`() {
        transaction {
            WingsDefinitionTable.deleteWhere { wingsDefinitionId eq wingsDefinitionId }
            PlayerWingsTable.deleteWhere { playerUuid eq playerUuid }

            WingsDefinitionTable.upsert {
                it[wingsDefinitionId] = "test-wings"
                it[itemModel] = "test-model"
            }

            PlayerWingsTable.upsert {
                it[playerUuid] = "test-uuid"
                it[wingsDefinitionId] = "test-wings"
            }
        }

        // Verify insertion
        transaction {
            assertEquals(1L, WingsDefinitionTable.selectAll().count())
            assertEquals(1L, PlayerWingsTable.selectAll().count())
        }

        // Delete from wings definition
        transaction {
            WingsDefinitionTable.deleteWhere { WingsDefinitionTable.wingsDefinitionId eq "test-wings" }
        }

        // Check if player wings are still there
        transaction {
            val playerWingsCount = PlayerWingsTable.selectAll().count()
            println("[DEBUG_LOG] Player wings count after delete: $playerWingsCount")
            // This is expected to FAIL (count will be 1) if the issue exists
            assertEquals(0L, playerWingsCount, "Player wings should have been deleted by cascade")
        }
    }
}
