package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisherBase
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.entity.Entity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AttributeSessionRegistry : AutoCloseable {

    val sessions: Map<UUID, AttributeSession>
        field = ConcurrentHashMap()

    operator fun get(uuid: UUID): AttributeSession? = sessions[uuid]

    operator fun get(entity: Entity): AttributeSession? = get(entity.uniqueId)

    fun getOrCreate(
        owner: Entity
    ): AttributeSession {
        return sessions.computeIfAbsent(owner.uniqueId) {
            AttributeSession(
                owner = owner,
                engine = BukkitAttributeEngine(
                    owner = owner,
                    parentScope = BukkitCoroutine,
                    publisher = AttributeEngineSnapshotPublisherBase()
                )
            )
        }
    }

    fun remove(ownerId: UUID): AttributeSession? =
        sessions.remove(ownerId)?.also { it.close() }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    override fun close() {
        val removed = sessions.values.toList()
        sessions.clear()
        for (session in removed) {
            session.close()
        }
    }

}
