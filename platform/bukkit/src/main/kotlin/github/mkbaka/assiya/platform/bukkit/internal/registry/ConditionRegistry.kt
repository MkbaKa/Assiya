package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry.eval
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import org.bukkit.entity.Entity

object ConditionRegistry : Registry<Condition>("条件") {

    fun register(condition: Condition) {
        register(condition.id, condition)
    }

    fun unregister(condition: Condition): Boolean {
        return unregister(condition.id, condition)
    }

    fun eval(entity: Entity, condition: ParsedCondition): Boolean {
        val id = condition.conditionId
        val impl = this[id] ?: return true

        return try {
            impl.check(entity, condition.context)
        } catch (e: Throwable) {
            logEx(e, "Failed to evaluate condition $id")
            false
        }
    }

}
