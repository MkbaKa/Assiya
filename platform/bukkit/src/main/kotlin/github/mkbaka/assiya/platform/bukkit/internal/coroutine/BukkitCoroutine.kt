package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

object BukkitCoroutine : AutoCloseable, CoroutineScope {

    private val async = Executors.newFixedThreadPool(
        (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    ).asCoroutineDispatcher()

    private val sync = ServerThreadDispatcher(AssiyaBukkitPlugin.INSTANCE)

    override val coroutineContext: CoroutineContext = SupervisorJob() + async + BukkitDispatchers(async, sync)

    @Awake(lifeCycle = LifeCycle.DISABLE)
    override fun close() {
        cancel()
        async.close()
    }

}
