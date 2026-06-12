package dev.codewithmike.wings.utils.dispatcher

import dev.codewithmike.wings.CWMWings
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext

class BukkitMainDispatcher(
    private val plugin: CWMWings
) : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Bukkit.getScheduler().runTask(plugin, block)
        }
    }
}