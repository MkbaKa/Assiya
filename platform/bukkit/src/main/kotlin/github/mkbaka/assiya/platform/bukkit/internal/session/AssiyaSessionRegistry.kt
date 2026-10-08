package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.entity.Entity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AssiyaSessionRegistry {

    val sessions: Map<UUID, AssiyaSession>
        field = ConcurrentHashMap()

    operator fun get(uuid: UUID): AssiyaSession? = sessions[uuid]

    operator fun get(entity: Entity): AssiyaSession? = get(entity.uniqueId)

    fun getOrCreate(
        owner: Entity
    ): AssiyaSession {
        return sessions.computeIfAbsent(owner.uniqueId) { AssiyaSession(owner) }
    }

    fun remove(ownerId: UUID): AssiyaSession? =
        sessions.remove(ownerId)?.also { it.close() }

    fun refresh() {
        for (session in sessions.values) {
            session.refresh()
        }
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    fun close() {
        val removed = sessions.values.toList()
        sessions.clear()
        for (session in removed) {
            session.close()
        }
    }

}