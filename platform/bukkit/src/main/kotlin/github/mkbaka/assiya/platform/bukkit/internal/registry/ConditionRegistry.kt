package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.condition.ParsedCondition
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import org.bukkit.entity.Entity

object ConditionRegistry : NamedRegistry<Condition>("条件") {

    override fun register(id: String, value: Condition) {
        super.register(id, value)
        try {
            AttributeRegistration.refresh()
        } catch (cause: Throwable) {
            super.unregister(id, value)
            throw cause
        }
    }

    override fun unregister(id: String, value: Condition): Boolean {
        if (!super.unregister(id, value)) return false
        AttributeRegistration.refresh()
        return true
    }

    fun register(condition: Condition) {
        register(condition.id, condition)
    }

    fun unregister(condition: Condition): Boolean {
        return unregister(condition.id, condition)
    }

    fun eval(
        entity: Entity,
        condition: ParsedCondition,
        context: ConditionContextImpl
    ): Boolean {
        val id = condition.conditionId
        val impl = this[id] ?: return true

        return try {
            impl.check(entity, context.withCaptures(condition.context.captures))
        } catch (e: Throwable) {
            logEx(e, "Failed to evaluate condition $id")
            false
        }
    }

}
