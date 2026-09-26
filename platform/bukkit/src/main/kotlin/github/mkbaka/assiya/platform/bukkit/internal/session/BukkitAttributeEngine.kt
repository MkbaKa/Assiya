package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.common.engine.DefaultAttributeEngine
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisher
import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeSourceRemoveEvent
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeUpdateEvent
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

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
        if (action.triggerEvent) {
            val entity = ownerEntity ?: return
            try {
                val event = AttributeUpdateEvent(entity, context)
                Bukkit.getPluginManager().callEvent(event)
                if (event.isCancelled) return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logEx(e, "Failed to process attribute update")
                return
            }
        }
        super.refresh(action, context)
    }

    override fun removeSource(
        sourceId: String,
        affectedAttributes: MutableSet<String>,
        changedSources: MutableSet<String>
    ) {
        val source = state.sourceIndex[sourceId] ?: return

        super.removeSource(
            sourceId,
            affectedAttributes,
            changedSources
        )

        val entity = ownerEntity ?: return
        try {
            Bukkit.getPluginManager().callEvent(AttributeSourceRemoveEvent(entity, source))
        } catch (e: Throwable) {
            logEx(e, "Failed to notify removal of source $sourceId")
        }
    }

}
