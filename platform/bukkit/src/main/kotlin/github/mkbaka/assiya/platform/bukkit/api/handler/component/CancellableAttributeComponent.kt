package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ForkableComponent
import github.mkbaka.assiya.common.handler.ProcessorComponent

/**
 * 由先执行属性控制后续属性的执行状态
 * 列表中的属性会被跳过执行
 *
 * @param [cancelled]
 */
class CancellableAttributeComponent(
    private val cancelled: MutableSet<String> = hashSetOf()
) : ProcessorComponent, ForkableComponent<CancellableAttributeComponent> {

    override val key: ProcessorComponent.Key<*> = Key

    fun isCancelled(attribute: String) = attribute in cancelled

    @JvmOverloads
    fun setCancelled(attribute: String, cancel: Boolean = true) {
        if (cancel) {
            cancelled.add(attribute)
        } else {
            cancelled.remove(attribute)
        }
    }

    override fun fork(): CancellableAttributeComponent = CancellableAttributeComponent(HashSet(cancelled))

    companion object Key : ProcessorComponent.Key<CancellableAttributeComponent>

}
