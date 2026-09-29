package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.common.engine.AttributeEngine
import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle

import org.bukkit.entity.Entity
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AttributeSession(
    owner: Entity,
    private val engine: AttributeEngine
) : AutoCloseable {

    var isClosed: Boolean = false
        private set

    val ownerId: UUID = owner.uniqueId
    private val ownerRef = WeakReference(owner)

    val owner: Entity?
        get() = ownerRef.get()

    val snapshot: AttributeEngineSnapshot
        get() = engine.snapshot()

    fun submitSnapshot(snapshot: AttributeEngineSnapshot) {
        val patches = arrayListOf<AttributeSourcePatch>()
        for ((sourceId, source) in snapshot.sources) {
            patches += AttributeSourcePatch(
                sourceId = sourceId,
                priority = source.priority,
                lifeCycle = AttributeSourceLifecycle.Stable,
                attributes = source.attributes
            )
        }
        submitPatches(patches)
    }

    fun submitPatches(patches: List<AttributeSourcePatch>) {
        val actions = arrayListOf<AttributeEngineAction>()
        for (patch in patches) {
            actions += AttributeEngineAction.UpdateSource(patch)
        }
        actions += AttributeEngineAction.Refresh()
        engine.push(*actions.toTypedArray())
    }

    fun submitRemove(sourceId: String) {
        engine.push(
            AttributeEngineAction.RemoveSource(sourceId),
            AttributeEngineAction.Refresh(triggerEvent = false)
        )
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        engine.close()
    }

}
