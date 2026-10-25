package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ForkableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent

/**
 * 整个 processor 的取消状态
 *
 * @param [isCancelled]
 */
class CancellableComponent(
    var isCancelled: Boolean = false
) : ProcessorComponent, ForkableComponent<CancellableComponent> {

    override val key: ProcessorComponent.Key<*> = Key

    override fun fork(): CancellableComponent = CancellableComponent(isCancelled)

    companion object Key : ProcessorComponent.Key<CancellableComponent>

}
