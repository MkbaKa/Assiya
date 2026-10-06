package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.common.engine.DefaultAttributeEngine
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisher
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeSourceRemoveEvent
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeUpdateEvent
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call
import kotlinx.coroutines.CoroutineScope
import org.bukkit.entity.Entity
import java.lang.ref.WeakReference
import java.util.*

class BukkitAttributeEngine(
    owner: Entity,
    parentScope: CoroutineScope,
    publisher: AttributeEngineSnapshotPublisher
) : DefaultAttributeEngine(
    parentScope, publisher, AssiyaBukkitPlugin.INSTANCE.logger
) {

    val owner: UUID = owner.uniqueId
    private val ownerRef = WeakReference(owner)

    val ownerEntity: Entity?
        get() = ownerRef.get()

    override fun refresh(
        action: AttributeEngineAction.Refresh,
        context: AttributeUpdateContext
    ) {
        val entity = ownerEntity ?: return

        if (action.triggerEvent && AttributeUpdateEvent.Pre(entity, context).call()) {
            return
        }

        super.refresh(action, context)

        if (action.triggerEvent) {
            AttributeUpdateEvent.Post(entity, context).call()
        }
    }

    override fun removeSource(
        sourceId: String,
        affectedAttributes: MutableSet<String>,
        changedSources: MutableSet<String>
    ) {
        val source = state.sourceIndex[sourceId] ?: return

        val entity = ownerEntity ?: return
        if (AttributeSourceRemoveEvent.Pre(entity, source).call()) {
            return
        }

        super.removeSource(
            sourceId,
            affectedAttributes,
            changedSources
        )

        AttributeSourceRemoveEvent.Post(entity, source).call()
    }

}
