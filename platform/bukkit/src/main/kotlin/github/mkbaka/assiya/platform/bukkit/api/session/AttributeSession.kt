package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.common.engine.AttributeEngine
import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.feature.AttributeReadPipeline
import org.bukkit.entity.Player

class AttributeSession(
    val assiyaSession: AssiyaSession,
    private val engine: AttributeEngine
) : AutoCloseable {

    var isClosed: Boolean = false
        private set

    val snapshot: AttributeEngineSnapshot
        get() = engine.snapshot()

    init {
        refreshDefaultAttributes()
    }

    fun refreshDefaultAttributes() {
        val owner = assiyaSession.owner ?: return

        val config = Config.global
        val parsed = if (owner is Player) config.DEFAULT_ATTRIBUTES_PLAYER else config.DEFAULT_ATTRIBUTES_ENTITY
        if (parsed == null) {
            AssiyaAPI.takeAttribute(assiyaSession, DEFAULT_SOURCE)
            return
        }
        val context = ConditionContextImpl(sourceId = DEFAULT_SOURCE)
        val attributes = AttributeReadPipeline.evaluate(owner, parsed, context)
        val patch = AttributeSourcePatch(
            sourceId = DEFAULT_SOURCE,
            priority = config.DEFAULT_ATTRIBUTES_PRIORITY,
            lifeCycle = AttributeSourceLifecycle.Stable,
            attributes = attributes
        )

        AssiyaAPI.addAttribute(assiyaSession, patch)
    }

    fun submit(vararg actions: AttributeEngineAction) {
        engine.push(*actions)
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
