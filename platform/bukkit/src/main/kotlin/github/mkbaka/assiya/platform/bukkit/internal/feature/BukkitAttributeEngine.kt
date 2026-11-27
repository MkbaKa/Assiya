package github.mkbaka.assiya.platform.bukkit.internal.feature

import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.common.engine.DefaultAttributeEngine
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisher
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeSourceRemoveEvent
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeUpdateEvent
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call
import kotlinx.coroutines.CoroutineScope

class BukkitAttributeEngine(
    val assiyaSession: AssiyaSession,
    parentScope: CoroutineScope,
    publisher: AttributeEngineSnapshotPublisher
) : DefaultAttributeEngine(
    parentScope, publisher, AssiyaBukkitPlugin.INSTANCE.logger
) {

    override fun refresh(
        action: AttributeEngineAction.Refresh,
        context: AttributeUpdateContext
    ): AttributeEngineSnapshot {
        if (action.triggerEvent && AttributeUpdateEvent.Pre(assiyaSession, context).call()) {
            return snapshot()
        }

        val snapshot = super.refresh(action, context)

        if (action.triggerEvent) {
            AttributeUpdateEvent.Post(assiyaSession, context).call()
        }
        return snapshot
    }

    override fun removeSource(
        sourceId: String,
        affectedAttributes: MutableSet<String>,
        changedSources: MutableSet<String>
    ) {
        val source = state.sourceIndex[sourceId] ?: return

        if (AttributeSourceRemoveEvent.Pre(assiyaSession, source).call()) {
            return
        }

        super.removeSource(
            sourceId,
            affectedAttributes,
            changedSources
        )

        AttributeSourceRemoveEvent.Post(assiyaSession, source).call()
    }

}
