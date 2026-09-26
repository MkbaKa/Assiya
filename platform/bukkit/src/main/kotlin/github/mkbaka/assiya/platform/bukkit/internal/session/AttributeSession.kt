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

    val damageProcessorStack = DamageProcessorStack()

    internal val cached = ConcurrentHashMap<String, AttributeParsedSource>()

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

//    // 自定义数据
    // TODO impl
//    private val data = ConcurrentHashMap<String, DataEntry>()
//
//    fun setData(key: String, value: Any?) {
//        data[key] = DataEntry(value, -1)
//    }
//
//    fun setData(key: String, value: Any?, durationMS: Long) {
//        data[key] = DataEntry(value, System.currentTimeMillis() + durationMS)
//    }
//
//    fun getData(key: String): Any? {
//        val entry = data[key] ?: return null
//        if (entry.expireAt != -1L && System.currentTimeMillis() > entry.expireAt) {
//            data.remove(key, entry)
//            return null
//        }
//        return entry.value
//    }
//
//    fun removeData(key: String) {
//        data.remove(key)
//    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        cached.clear()
        engine.close()
    }

//    private class DataEntry(
//        val value: Any?,
//        val expireAt: Long,
//    )

}
