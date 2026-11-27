package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.event.AssiyaSessionInitEvent
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.entity.Entity
import java.util.*

object AssiyaSessionRegistry : Registry<UUID, AssiyaSession>() {

    val sessions: Map<UUID, AssiyaSession>
        get() = elements

    public override operator fun get(key: UUID): AssiyaSession? = super.get(key)

    operator fun get(entity: Entity): AssiyaSession? = get(entity.uniqueId)

    fun getOrCreate(
        owner: Entity
    ): AssiyaSession {
        return elements.computeIfAbsent(owner.uniqueId) {
            AssiyaSession(owner).also { AssiyaSessionInitEvent(it, owner).call() }
        }
    }

    fun remove(ownerId: UUID): AssiyaSession? =
        elements.remove(ownerId)?.also { it.close() }

    fun remove(session: AssiyaSession): Boolean =
        elements.remove(session.ownerUUID, session)

    fun refresh() {
        for (session in sessions.values) {
            session.refresh()
        }
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    fun close() {
        val removed = sessions.values.toList()
        elements.clear()
        for (session in removed) {
            session.close()
        }
    }

}
