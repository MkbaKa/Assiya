package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.common.engine.AttributeEngine
import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigGlobal
import github.mkbaka.assiya.platform.bukkit.internal.parser.AttributeReadPipeline
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.*

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

    init {
        refreshDefaultAttributes()
    }

    fun refreshDefaultAttributes(config: ConfigGlobal = Config.global) {
        val owner = owner ?: return
        val parsed = if (owner is Player) config.DEFAULT_ATTRIBUTES_PLAYER else config.DEFAULT_ATTRIBUTES_ENTITY
        if (parsed == null) {
            submitRemove(DEFAULT_SOURCE)
            return
        }
        val attributes = AttributeReadPipeline.evaluateSync(owner, parsed)
        val patch = AttributeSourcePatch(
            sourceId = DEFAULT_SOURCE,
            priority = config.DEFAULT_ATTRIBUTES_PRIORITY,
            lifeCycle = AttributeSourceLifecycle.Stable,
            attributes = attributes
        )

        submitPatches(false, patch)
    }

    @JvmOverloads
    fun submitSnapshot(
        snapshot: AttributeEngineSnapshot,
        lifeCycle: AttributeSourceLifecycle = AttributeSourceLifecycle.Stable
    ) {
        val patches = arrayListOf<AttributeSourcePatch>()
        for ((sourceId, source) in snapshot.sources) {
            patches += AttributeSourcePatch(
                sourceId = sourceId,
                priority = source.priority,
                lifeCycle = lifeCycle,
                attributes = source.attributes
            )
        }

        submitPatches(false, *patches.toTypedArray())
    }

    @JvmOverloads
    fun submitPatches(triggerEvent: Boolean = true, vararg patches: AttributeSourcePatch) {
        val actions = arrayListOf<AttributeEngineAction>()
        for (patch in patches) {
            actions += AttributeEngineAction.UpdateSource(patch)
        }
        actions += AttributeEngineAction.Refresh(triggerEvent)
        engine.push(*actions.toTypedArray())
    }

    fun submitRemove(sourceId: String) {
        engine.push(
            AttributeEngineAction.RemoveSource(sourceId),
            AttributeEngineAction.Refresh(triggerEvent = false)
        )
    }

    fun submit(actions: List<AttributeEngineAction>) {
        engine.push(AttributeEngineAction.Batch(actions))
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        engine.close()
    }

    companion object {

        const val DEFAULT_SOURCE = "assiya:default-attributes"

    }

}
