package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

object SchedulerUtils {

    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) = BukkitCoroutine.launch(context, start, block)

    suspend fun <T> withSync(block: suspend CoroutineScope.() -> T): T {
        val dispatchers = currentCoroutineContext()[BukkitDispatchers]
            ?: error("Bukkit dispatcher is unavailable")

        return withContext(dispatchers.sync, block)
    }

    suspend fun <T> withAsync(block: suspend CoroutineScope.() -> T): T {
        val dispatchers = currentCoroutineContext()[BukkitDispatchers]
            ?: error("Bukkit dispatcher is unavailable")

        return withContext(dispatchers.async, block)
    }

    suspend fun delayTicks(ticks: Long) {
        if (ticks <= 0) return

        suspendCancellableCoroutine { continuation ->
            val task = callAsyncLater(ticks) {
                continuation.resume(Unit)
            }
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    @JvmStatic
    fun callSync(runnable: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run()
        } else {
            Bukkit.getScheduler().runTask(AssiyaBukkitPlugin.INSTANCE, runnable)
        }
    }

    @JvmStatic
    fun callAsync(runnable: Runnable) {
        if (!Bukkit.isPrimaryThread()) {
            runnable.run()
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(AssiyaBukkitPlugin.INSTANCE, runnable)
        }
    }

    @JvmStatic
    fun callSyncLater(delay: Long, runnable: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskLater(AssiyaBukkitPlugin.INSTANCE, runnable, delay)
    }

    @JvmStatic
    fun callAsyncLater(delay: Long, runnable: Runnable): BukkitTask {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(AssiyaBukkitPlugin.INSTANCE, runnable, delay)
    }

    @JvmStatic
    fun callSyncTimer(
        delay: Long,
        period: Long,
        runnable: Runnable
    ): BukkitTask {
        return Bukkit.getScheduler().runTaskTimer(
            AssiyaBukkitPlugin.INSTANCE,
            runnable,
            delay,
            period
        )
    }

    @JvmStatic
    fun callAsyncTimer(
        delay: Long,
        period: Long,
        runnable: Runnable
    ): BukkitTask {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(
            AssiyaBukkitPlugin.INSTANCE,
            runnable,
            delay,
            period
        )
    }

}
