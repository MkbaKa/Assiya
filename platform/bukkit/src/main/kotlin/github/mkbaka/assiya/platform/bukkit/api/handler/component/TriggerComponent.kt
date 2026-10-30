package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.EntityProcessorComponent
import org.bukkit.entity.Entity

/**
 * 触发 processor 的来源
 *
 * @param [entity]
 * @param [snapshot]
 */
class TriggerComponent @JvmOverloads constructor(
    override val entity: Entity,
    override val snapshot: AttributeEngineSnapshot,
    attribute: AttributeContainer = DefaultAttributeContainer(snapshot)
) : ProcessorComponent, EntityProcessorComponent, AttributeContainer by attribute {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<TriggerComponent>

}
