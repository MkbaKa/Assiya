package github.mkbaka.assiya.platform.bukkit.internal.registry

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.assiya.platform.bukkit.api.task.EntityTask
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object EntityTaskRegistry : Registry<UUID, MutableMap<Any, EntityTask>>() {

    fun add(ownerId: UUID, task: EntityTask) {
        var previous: EntityTask? = null
        elements.compute(ownerId) { _, current ->
            val owned = current ?: hashMapOf()
            previous = owned.put(task.key ?: task, task)
            owned
        }
        previous?.replace()
    }

    fun cancel(ownerId: UUID, key: String) {
        var task: EntityTask? = null
        elements.computeIfPresent(ownerId) { _, owned ->
            task = owned.remove(key)
            if (owned.isEmpty()) null else owned
        }
        task?.cancel()
    }

    fun remove(ownerId: UUID, task: EntityTask) {
        elements.computeIfPresent(ownerId) { _, owned ->
            owned.remove(task.key ?: task, task)
            if (owned.isEmpty()) null else owned
        }
    }

    private fun cancel(ownerId: UUID) {
        val removed = elements.remove(ownerId) ?: return
        for (task in removed.values) {
            task.cancel()
        }
    }

    @Listener
    fun onQuit(event: PlayerQuitEvent) {
        cancel(event.player.uniqueId)
    }

    @Listener(priority = EventPriority.MONITOR)
    fun onRemove(event: EntityRemoveFromWorldEvent) {
        if (event.entity is Player) return
        cancel(event.entity.uniqueId)
    }

    @Awake(lifeCycle = LifeCycle.DISABLE, priority = 200)
    fun close() {
        for (ownerId in elements.keys) {
            cancel(ownerId)
        }
    }

}
