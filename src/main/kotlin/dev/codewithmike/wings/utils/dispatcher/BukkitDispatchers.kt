package dev.codewithmike.wings.utils.dispatcher

import dev.codewithmike.wings.CWMWings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object BukkitDispatchers {

    private var mainDispatcher: CoroutineDispatcher? = null

    fun init() {
        mainDispatcher = BukkitMainDispatcher(CWMWings.instance)
    }

    val main: CoroutineDispatcher
        get() = checkNotNull(mainDispatcher) {
            "BukkitDispatchers has not been initialized"
        }
    val async: CoroutineDispatcher = Dispatchers.IO
}
