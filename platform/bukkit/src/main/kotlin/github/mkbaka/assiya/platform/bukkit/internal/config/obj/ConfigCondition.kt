package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.common.condition.ConditionContext
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.internal.config.ScriptManager

import github.mkbaka.assiya.platform.bukkit.internal.util.warning
import org.bukkit.entity.Entity

class ConfigCondition(
    override val id: String,
    override val names: List<String>,
    override val reader: String,
    val handler: String,
) : Condition {

    override fun check(entity: Entity, context: ConditionContext): Boolean {
        val script = ScriptManager[handler]
        if (script == null) {
            warning("Handler script $handler for condition $id was not found")
            return true
        }

        val result = script.invokeSafely("check", entity, context)
        return result as? Boolean
            ?: throw IllegalStateException(
                "condition $id's handler returned a non-Boolean value: $result"
            )
    }

}
