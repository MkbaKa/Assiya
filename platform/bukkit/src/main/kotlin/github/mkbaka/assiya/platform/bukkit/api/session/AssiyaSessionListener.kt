package github.mkbaka.assiya.platform.bukkit.api.session

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.misc.Unregister
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine.delayTicks
import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.registry.AssiyaSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
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

    private var joinUnregister: Unregister? = null
    private var quitUnregister: Unregister? = null

    @CustomTask(taskId = "reload", priority = 3)
    fun reload() {
        unregisterListeners()
        if (!Config.global.PLAYER_SESSION_LIFECYCLE) return

        joinUnregister = EventUtils.on<PlayerJoinEvent> {
            if (Config.global.HEALTH_SCALE) {
                player.healthScale = Config.global.HEALTH_SCALE_VALUE
            }

            while (true) {
                val session = AssiyaSessionRegistry.getOrCreate(player)
                val bound = synchronized(timeoutJobs) {
                    if (AssiyaSessionRegistry[player.uniqueId] !== session) {
                        false
                    } else {
                        timeoutJobs.remove(player.uniqueId)?.cancel()
                        session.bindOwner(player)
                        true
                    }
                }
                if (bound) break
            }
            AssiyaAPI.getSessionOrCreate(player)
            AssiyaAPI.refreshEquipment(player)
        }
        quitUnregister = EventUtils.on<PlayerQuitEvent> {
            val session = AssiyaSessionRegistry[player.uniqueId] ?: return@on
            synchronized(timeoutJobs) {
                if (session.owner !== player) return@on
                session.unbindOwner(player)
            }
            EquipmentPipeline.cancel(player.uniqueId)
            val timeout = Config.global.PLAYER_SESSION_TIMEOUT
            if (timeout <= 0L) {
                val removed = synchronized(timeoutJobs) {
                    session.owner == null && AssiyaSessionRegistry.remove(session)
                }
                if (removed) session.close()
            } else {
                scheduleTimeout(session, timeout)
            }
        }
    }

    private fun scheduleTimeout(session: AssiyaSession, timeout: Long) {
        val ownerId = session.ownerUUID
        val job = BukkitCoroutine.launch(
            context = CoroutineExceptionHandler { _, cause ->
                logEx(cause, "Failed to expire session for $ownerId")
            },
            start = CoroutineStart.LAZY
        ) {
            delayTicks(timeout * 20L)
            ensureActive()
            val task = currentCoroutineContext().job
            val removed = synchronized(timeoutJobs) {
                task.isActive && timeoutJobs[ownerId] === task && session.owner == null &&
                    AssiyaSessionRegistry.remove(session)
            }
            if (removed) session.close()
        }
        job.invokeOnCompletion {
            timeoutJobs.remove(ownerId, job)
        }

        timeoutJobs.put(ownerId, job)?.cancel()
        job.start()
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
