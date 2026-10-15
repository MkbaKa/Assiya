package github.mkbaka.assiya.platform.bukkit.api

import github.mkbaka.assiya.platform.bukkit.api.task.EntityTask
import github.mkbaka.assiya.platform.bukkit.api.task.EntityTaskBuilder
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import github.mkbaka.assiya.platform.bukkit.internal.registry.EntityTaskRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.delayTicks
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.launch
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import org.bukkit.entity.Entity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer
import java.util.function.Consumer

object EntityTaskAPI {

    private val tasks = ConcurrentHashMap<String, PresetTask>()

    @JvmStatic
    @JvmOverloads
    fun registerTask(
        taskId: String,
        task: BiConsumer<Entity, Map<String, Any>>,
        onTerminate: BiConsumer<Entity, Map<String, Any>>? = null
    ) {
        tasks[taskId] = PresetTask(task, onTerminate)
    }

    @JvmStatic
    fun cancelTask(owner: Entity, key: String) {
        EntityTaskRegistry.cancel(owner.uniqueId, key)
    }

    @JvmStatic
    @JvmOverloads
    fun runTask(
        owner: Entity,
        taskId: String,
        builder: Consumer<EntityTaskBuilder> = Consumer {}
    ): EntityTask {
        val task = tasks[taskId] ?: error("Entity task '$taskId' is not registered")

        val options = EntityTaskBuilder()
        builder.accept(options)

        val key = options.key
        val delay = options.delay
        val period = options.period
        val count = options.count
        val async = options.async
        val params = options.params

        val dispatchers = BukkitCoroutine.coroutineContext[BukkitDispatchers]!!
        val job = launch(
            context = if (async) dispatchers.async else dispatchers.sync,
            start = CoroutineStart.LAZY
        ) {
            if (delay > 0) {
                delayTicks(delay)
            }

            var executed = 0
            while (count != null && executed < count) {
                ensureActive()
                task.action.accept(owner, params)
                executed++

                if (period > 0 && executed < count) {
                    delayTicks(period)
                }
            }
        }

        val entityTask = EntityTask(owner.uniqueId, taskId, job, key) {
            task.onTerminate?.accept(owner, params)
        }
        EntityTaskRegistry.add(entityTask.ownerId, entityTask)
        job.invokeOnCompletion { cause ->
            entityTask.finish(cause != null)
        }
        job.start()
        return entityTask
    }

    private class PresetTask(
        val action: BiConsumer<Entity, Map<String, Any>>,
        val onTerminate: BiConsumer<Entity, Map<String, Any>>?
    )

}
