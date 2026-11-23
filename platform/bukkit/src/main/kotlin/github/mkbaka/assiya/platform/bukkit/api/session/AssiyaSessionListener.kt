package github.mkbaka.assiya.platform.bukkit.api.session

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitDispatchers
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.CustomTask
import github.mkbaka.bukkitksp.annotation.LifeCycle
import github.mkbaka.bukkitksp.annotation.Listener
import kotlinx.coroutines.*
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AssiyaSessionListener {

    private val timeoutJobs = ConcurrentHashMap<UUID, Job>()

    private var joinUnregister: EventUtils.Unregister? = null
    private var quitUnregister: EventUtils.Unregister? = null

    @CustomTask(taskId = "reload", priority = 3)
    fun reload() {
        SchedulerUtils.callSync {

            unregisterListeners()
            if (!Config.global.PLAYER_SESSION_LIFECYCLE) return@callSync

            joinUnregister = EventUtils.on<PlayerJoinEvent> {
                timeoutJobs.remove(player.uniqueId)?.cancel()
                if (Config.global.HEALTH_SCALE) {
                    player.healthScale = Config.global.HEALTH_SCALE_VALUE
                }

                AssiyaAPI.getSessionOrCreate(player).bindOwner(player)
                AssiyaAPI.refreshEquipment(player)
            }
            quitUnregister = EventUtils.on<PlayerQuitEvent> {
                val ownerId = player.uniqueId
                val session = AssiyaSessionRegistry[ownerId]
                session?.unbindOwner(player)
                val timeout = Config.global.PLAYER_SESSION_TIMEOUT
                if (timeout <= 0L) {
                    AssiyaSessionRegistry.remove(ownerId)
                    return@on
                }

                timeoutJobs.remove(ownerId)?.cancel()

                val job = SchedulerUtils.launch(
                    context = BukkitCoroutine.coroutineContext[BukkitDispatchers.Key]!!.sync,
                    start = CoroutineStart.LAZY
                ) {
                    SchedulerUtils.delayTicks(timeout * 20L)
                    ensureActive()

                    if (session != null && AssiyaSessionRegistry[ownerId] === session && session.owner == null) {
                        AssiyaSessionRegistry.remove(ownerId)
                    }

                    timeoutJobs.remove(ownerId, currentCoroutineContext().job)
                }

                timeoutJobs[ownerId] = job
            }

        }
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    fun close() {
        for (job in timeoutJobs.values) {
            job.cancel()
        }
        timeoutJobs.clear()
        unregisterListeners()
    }

    private fun unregisterListeners() {
        joinUnregister?.unregister()
        quitUnregister?.unregister()
        joinUnregister = null
        quitUnregister = null
    }

    @Listener(priority = EventPriority.MONITOR)
    fun onRemove(event: EntityRemoveFromWorldEvent) {
        val entity = event.entity
        if (entity is Player) return

        AssiyaSessionRegistry.remove(event.entity.uniqueId)
    }

}