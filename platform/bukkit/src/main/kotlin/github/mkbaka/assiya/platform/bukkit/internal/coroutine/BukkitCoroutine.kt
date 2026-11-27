package github.mkbaka.assiya.platform.bukkit.internal.coroutine

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.compat.FoliaCompat
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

object BukkitCoroutine : AutoCloseable, CoroutineScope {

    val async = Executors.newFixedThreadPool(
        Config.global.ASYNC_THREADS.let { threads ->
            if (threads == 0) (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1) else threads
        }
    ).asCoroutineDispatcher()

    override val coroutineContext: CoroutineContext = SupervisorJob() + async

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = (this as CoroutineScope).launch(context, start, block)

    fun launch(
        entity: Entity,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = launch(context + EntityContext(entity) + EntityDispatcher, start, block)

    fun launch(
        location: Location,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = launch(context + RegionContext(location) + RegionDispatcher, start, block)

    suspend inline fun <T> withAsync(noinline block: suspend CoroutineScope.() -> T): T {
        return withContext(async, block)
    }

    suspend fun delayTicks(ticks: Long) {
        if (ticks <= 0) return

        if (FoliaCompat.isFolia()) {
            when (val target = currentCoroutineContext()[SchedulerContext]) {
                is RegionContext -> {
                    suspendCancellableCoroutine { continuation ->
                        val cancel = FoliaCompat.scheduleRegion(
                            target.world, target.chunkX, target.chunkZ, ticks
                        ) { continuation.resume(Unit) }
                        continuation.invokeOnCancellation { cancel.run() }
                    }
                }

                is EntityContext -> {
                    suspendCancellableCoroutine { continuation ->
                        val cancel = FoliaCompat.scheduleEntity(
                            target.entity, ticks,
                            { continuation.resume(Unit) },
                            { continuation.cancel(CancellationException("Entity scheduler was retired")) }
                        )
                        continuation.invokeOnCancellation { cancel.run() }
                    }
                }

                null -> {
                    suspendCancellableCoroutine { continuation ->
                        val cancel = FoliaCompat.scheduleGlobal(ticks, -1L) {
                            continuation.resume(Unit)
                        }
                        continuation.invokeOnCancellation { cancel.run() }
                    }
                }
            }
            return
        }

        suspendCancellableCoroutine { continuation ->
            val task = Bukkit.getScheduler().runTaskLaterAsynchronously(AssiyaBukkitPlugin.INSTANCE, Runnable {
                continuation.resume(Unit)
            }, ticks)
            continuation.invokeOnCancellation { task.cancel() }
        }
    }

    @JvmStatic
    fun callEntityLater(entity: Entity, delayTicks: Long, runnable: Runnable): Job {
        return launch(entity, CoroutineExceptionHandler { _, cause ->
            logEx(cause, "Failed to execute delayed entity task")
        }) {
            delayTicks(delayTicks)

            // isValid 会排除死亡玩家, 但死亡掉落后仍需刷新装备.
            if (entity is Player && !entity.isOnline) {
                throw CancellationException("Entity is no longer available")
            }

            runnable.run()
        }
    }

    @Awake(lifeCycle = LifeCycle.DISABLE, priority = 114514)
    override fun close() {
        cancel()
        async.close()
    }

}
