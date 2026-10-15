package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.platform.bukkit.internal.registry.Registry
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
        return elements.computeIfAbsent(owner.uniqueId) { AssiyaSession(owner) }
    }

    fun remove(ownerId: UUID): AssiyaSession? =
        elements.remove(ownerId)?.also { it.close() }

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
