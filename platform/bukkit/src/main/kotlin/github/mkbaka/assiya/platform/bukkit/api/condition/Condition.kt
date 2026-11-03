package github.mkbaka.assiya.platform.bukkit.api.condition

import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import org.bukkit.entity.Entity

interface Condition {

    /**
     * 索引名
     */
    val id: String

    /**
     * 读取时判断的条件名
     */
    val names: List<String>

    /**
     * 读取规则
     */
    val reader: String

    val components: Map<ConditionComponent.Key<*>, ConditionComponent>

    @Suppress("UNCHECKED_CAST")
    operator fun <C : ConditionComponent> get(key: ConditionComponent.Key<C>): C? {
        return components[key] as? C
    }

    /**
     * 条件逻辑
     */
    fun check(entity: Entity, context: ConditionContextImpl): Boolean

    fun register() {
        ConditionRegistry.register(this)
    }

    fun unregister() {
        ConditionRegistry.unregister(this)
    }

}
