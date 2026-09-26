package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent

/**
 * 整个 processor 的取消状态
 *
 * @param [isCancelled]
 */
class CancellableComponent(
    var isCancelled: Boolean = false
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<CancellableComponent>

}