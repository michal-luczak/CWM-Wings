package dev.codewithmike.wings.wing

import dev.codewithmike.wings.utils.text.sendFailureMessage
import dev.codewithmike.wings.utils.text.sendInfoMessage
import dev.codewithmike.wings.utils.text.sendSuccessMessage
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Sender
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.shortcut.Shortcut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command(name = "wings")
class WingsCommand(
    private val wingsManager: WingsManager,
    private val scope: CoroutineScope,
) {

    @Execute
    fun helpDefault(@Sender sender: CommandSender) {
        showHelp(sender)
    }

    @Execute(name = "help")
    fun help(@Sender sender: CommandSender) {
        showHelp(sender)
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendInfoMessage("Wings Help:")
        sender.sendMessage(Component.text("/wings on - Turn on your wings (/won)", NamedTextColor.GRAY))
        sender.sendMessage(Component.text("/wings off - Turn off your wings (/woff)", NamedTextColor.GRAY))

        if (sender.hasPermission("wings.command.wings")) {
            sender.sendMessage(Component.text("/wings create <name> <item_model> - Create wings (/wc)", NamedTextColor.GRAY))
            sender.sendMessage(Component.text("/wings delete <name> - Delete wings (/wd)", NamedTextColor.GRAY))
            sender.sendMessage(Component.text("/wings grant <name> <player> - Grant wings (/wg)", NamedTextColor.GRAY))
            sender.sendMessage(Component.text("/wings revoke <player> - Revoke wings (/wr)", NamedTextColor.GRAY))
            sender.sendMessage(Component.text("/wings reload - Reload wings (/wrl)", NamedTextColor.GRAY))
        }
    }

    @Shortcut("woff")
    @Execute(name = "off")
    fun turnOffWings(@Sender sender: Player) {
        scope.launch {
            val doesPlayerHaveWings = wingsManager.doesPlayerHaveWings(sender)
            if (doesPlayerHaveWings) {
                wingsManager.turnOffWings(sender.uniqueId)
                wingsManager.despawnWingsByPlayerUuid(sender.uniqueId)
                sender.sendSuccessMessage("Your wings has been successfully turned off.")
            } else {
                sender.sendFailureMessage("You don't have any wings!")
            }
        }
    }

    @Shortcut("won")
    @Execute(name = "on")
    fun turnOnWings(@Sender sender: Player) {
        scope.launch {
            val doesPlayerHaveWings = wingsManager.doesPlayerHaveWings(sender)
            if (doesPlayerHaveWings) {
                wingsManager.turnOnWings(sender.uniqueId)
                wingsManager.spawnWings(sender)
                sender.sendSuccessMessage("Your wings has been successfully turned on.")
            } else {
                sender.sendFailureMessage("You don't have any wings!")
            }
        }
    }
}
