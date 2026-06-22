package dev.codewithmike.wings.wing

import dev.codewithmike.wings.data.definition.WingsDefinitionDto
import dev.codewithmike.wings.utils.text.sendFailureMessage
import dev.codewithmike.wings.utils.text.sendLoadingMessage
import dev.codewithmike.wings.utils.text.sendSuccessMessage
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.context.Sender
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import dev.rollczi.litecommands.annotations.shortcut.Shortcut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

@Suppress("Unused")
@Command(name = "wings", aliases = ["w"])
@Permission("wings.command.wings")
class WingsAdminCommand(
    private val wingsManager: WingsManager,
    private val scope: CoroutineScope,
) {
    @Execute(name = "create")
    @Shortcut("wc")
    fun create(
        @Sender sender: Player,
        @Arg("wings") wings: String,
        @Arg("itemModel") itemModel: String,
    ) {
        sender.sendLoadingMessage("Creating wings $wings with item model $itemModel...")
        scope.launch {
            val result = wingsManager.createWingsDefinition(wings, itemModel)
            when (result.result) {
                WingsResult.WingsResultType.SUCCESS -> {
                    sender.sendSuccessMessage("Successfully given wings!")
                }
                WingsResult.WingsResultType.FAILURE -> {
                    sender.sendFailureMessage("Error: ${result.reason}")
                }
            }
        }
    }

    @Shortcut("wd")
    @Execute(name = "delete")
    fun delete(
        @Sender sender: Player,
        @Arg("wings")
        wings: WingsDefinitionDto,
    ) {
        sender.sendLoadingMessage("Deleting wings ${wings.wingsDefinitionId}...")
        scope.launch {
            wingsManager.deleteWingsDefinition(wings.wingsDefinitionId)
        }
    }

    @Shortcut("wg")
    @Execute(name = "grant")
    fun grant(
        @Sender sender: Player,
        @Arg("wings") wings: WingsDefinitionDto,
        @Arg targetPlayer: OfflinePlayer,
    ) {
        sender.sendLoadingMessage("Giving wings ${wings.wingsDefinitionId} to ${targetPlayer.name}...")
        scope.launch {
            val result =
                wingsManager.giveWingsToPlayer(
                    wings.wingsDefinitionId,
                    targetPlayer,
                )
            when (result.result) {
                WingsResult.WingsResultType.SUCCESS -> {
                    sender.sendSuccessMessage("Successfully given wings!")
                }
                WingsResult.WingsResultType.FAILURE -> {
                    sender.sendFailureMessage("Error: ${result.reason}")
                }
            }
        }
    }

    @Shortcut("wr")
    @Execute(name = "revoke")
    fun revoke(
        @Sender sender: Player,
        @Arg targetPlayer: OfflinePlayer,
    ) {
        sender.sendLoadingMessage("Removing wings from ${targetPlayer.name}...")
        scope.launch {
            val result = wingsManager.removeWingsFromPlayer(targetPlayer)
            when (result.result) {
                WingsResult.WingsResultType.SUCCESS -> {
                    sender.sendSuccessMessage("Successfully removed wings!")
                }
                WingsResult.WingsResultType.FAILURE -> {
                    sender.sendFailureMessage("Error: ${result.reason}")
                }
            }
        }
    }

    @Shortcut("wrl")
    @Execute(name = "reload")
    fun reload(
        @Context sender: Player,
    ) {
        sender.sendLoadingMessage("Reloading wings...")
        wingsManager.despawnAllWings()
        scope.launch {
            wingsManager.respawnAllWings()
            sender.sendSuccessMessage("Config has been reloaded!")
        }
    }

    data class WingsResult(
        val reason: String? = null,
        val result: WingsResultType,
    ) {
        companion object {
            fun failure(reason: String): WingsResult = WingsResult(reason, WingsResultType.FAILURE)

            fun success(): WingsResult = WingsResult(result = WingsResultType.SUCCESS)
        }

        enum class WingsResultType {
            SUCCESS,
            FAILURE,
        }
    }
}
