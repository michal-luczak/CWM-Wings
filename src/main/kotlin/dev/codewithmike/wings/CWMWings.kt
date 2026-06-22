package dev.codewithmike.wings

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.codewithmike.wings.data.definition.WingsDefinitionDto
import dev.codewithmike.wings.utils.dispatcher.BukkitDispatchers
import dev.codewithmike.wings.utils.text.failureComponent
import dev.codewithmike.wings.wing.WingsAdminCommand
import dev.codewithmike.wings.wing.WingsCommand
import dev.codewithmike.wings.wing.WingsDefinitionArgumentResolver
import dev.codewithmike.wings.wing.WingsListener
import dev.codewithmike.wings.wing.WingsManager
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.adventure.LiteAdventureExtension
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import dev.rollczi.litecommands.message.LiteMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

class CWMWings : JavaPlugin() {
    private lateinit var wingsManager: WingsManager
    private lateinit var liteCommands: LiteCommands<CommandSender>
    private lateinit var scope: CoroutineScope

    companion object {
        lateinit var instance: CWMWings private set
    }

    override fun onEnable() {
        instance = this
        BukkitDispatchers.init()
        scope =
            CoroutineScope(
                SupervisorJob() + Dispatchers.Default,
            )
        wingsManager = WingsManager()

        val dbConfig =
            HikariConfig().apply {
                if (!dataFolder.exists()) dataFolder.mkdirs()
                jdbcUrl = "jdbc:sqlite:$dataFolder/database.db"
                maximumPoolSize = 5
                poolName = "MyPluginPool"
                connectionInitSql = "PRAGMA foreign_keys = ON"
            }
        this.liteCommands =
            LiteBukkitFactory
                .builder("cwm-wings", this)
                .commands(
                    WingsAdminCommand(wingsManager, scope),
                    WingsCommand(wingsManager, scope),
                ).argument(WingsDefinitionDto::class.java, WingsDefinitionArgumentResolver(wingsManager, scope))
                .message(LiteMessages.INVALID_USAGE, failureComponent("Invalid command usage. Type /wings help"))
                .extension(LiteAdventureExtension())
                .build()

        val dataSource = HikariDataSource(dbConfig)
        Flyway
            .configure(classLoader)
            .dataSource(dataSource)
            .locations("db/migration")
            .load()
            .migrate()
        Database.connect(dataSource)
        getPluginManager().registerEvents(WingsListener(wingsManager, scope), this)
    }

    override fun onDisable() {
        scope.cancel()
        liteCommands.unregister()
        wingsManager.despawnAllWings()
    }
}
