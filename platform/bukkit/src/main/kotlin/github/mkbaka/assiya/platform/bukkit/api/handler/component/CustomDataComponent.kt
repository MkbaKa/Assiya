package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import java.util.concurrent.ConcurrentHashMap

/**
 * 允许在 processor 执行过程中跨属性传递数据
 */
class CustomDataComponent(
    private val delegate: MutableMap<String, Any?> = hashMapOf()
) : ProcessorComponent, MutableMap<String, Any?> by delegate {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<CustomDataComponent>

}
