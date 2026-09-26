package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.ExecutorService
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class BukkitDispatchers(
    val async: CoroutineDispatcher,
    val sync: ServerThreadDispatcher
) : AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<BukkitDispatchers>

}

class ServerThreadDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return !Bukkit.isPrimaryThread()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(plugin, block)
    }

}