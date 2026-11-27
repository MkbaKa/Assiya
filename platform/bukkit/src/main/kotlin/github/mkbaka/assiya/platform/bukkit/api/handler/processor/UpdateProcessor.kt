package github.mkbaka.assiya.platform.bukkit.api.handler.processor

import github.mkbaka.assiya.common.engine.AttributeUpdateContext
import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import kotlinx.coroutines.CoroutineScope
import org.bukkit.entity.Entity
import java.util.function.Consumer

class UpdateProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
    val session: AssiyaSession,
    val context: AttributeUpdateContext,
    attribute: AttributeContainer = DefaultAttributeContainer(context.workingState)
) : BukkitProcessor, AttributeContainer by attribute {

    constructor(
        session: AssiyaSession,
        context: AttributeUpdateContext,
        pcb: Consumer<ProcessorComponentBuilder>
    ) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build(),
        session,
        context
    )

    override fun process() {
        HandlerRegistry.publish("update", this)
        this[CallbackComponent]?.invoke(this)
    }

    fun <E : Entity> launch(entity: E, action: Consumer<E>) = BukkitCoroutine.launch(entity) {
        action.accept(entity)
    }

    fun launch(entity: Entity, block: suspend CoroutineScope.() -> Unit) = BukkitCoroutine.launch(entity, block = block)

    companion object {

        @JvmStatic
        fun of(
            session: AssiyaSession,
            context: AttributeUpdateContext
        ): UpdateProcessor {
            return UpdateProcessor(session, context) { pcb ->
                pcb += BukkitProcessor.defaults()
                val entity = session.owner
                if (entity != null) {
                    pcb += TriggerComponent(
                        entity,
                        session.attribute.snapshot,
                        DefaultAttributeContainer(context.workingState)
                    )
                }
            }
        }

    }

}
