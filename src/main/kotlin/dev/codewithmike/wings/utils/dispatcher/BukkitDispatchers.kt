package dev.codewithmike.wings.utils.dispatcher

import dev.codewithmike.wings.CWMWings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object BukkitDispatchers {

    private var mainDispatcher: CoroutineDispatcher? = null

    fun init(plugin: CWMWings) {
        mainDispatcher = BukkitMainDispatcher(plugin)
    }

    val main: CoroutineDispatcher
        get() = checkNotNull(mainDispatcher) {
            "BukkitDispatchers has not been initialized"
        }
    val async: CoroutineDispatcher = Dispatchers.IO
}
