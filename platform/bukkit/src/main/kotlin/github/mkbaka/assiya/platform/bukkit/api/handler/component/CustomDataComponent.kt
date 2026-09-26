package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent

/**
 * 允许在 processor 执行过程中跨属性传递数据
 *
 * @param [data]
 */
class CustomDataComponent(
    private val data: MutableMap<String, Any?> = hashMapOf()
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    operator fun set(name: String, value: Any?): Any? = data.put(name, value)

    operator fun get(name: String): Any? = data[name]

    companion object Key : ProcessorComponent.Key<CancellableAttributeComponent>

}