package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionComponent
import github.mkbaka.assiya.platform.bukkit.api.condition.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.internal.config.ScriptManager

import github.mkbaka.assiya.platform.bukkit.internal.util.warning
import org.bukkit.entity.Entity

class ConfigCondition(
    override val id: String,
    override val names: List<String>,
    override val reader: String,
    val handler: String,
    options: OptionsComponent? = null,
) : Condition {

    override val components: Map<ConditionComponent.Key<*>, ConditionComponent> =
        if (options == null) emptyMap() else mapOf(options.key to options)

    override fun check(entity: Entity, context: ConditionContextImpl): Boolean {
        val script = ScriptManager[handler]
        if (script == null) {
            warning("Handler script $handler for condition $id was not found")
            return true
        }

        val result = script.invokeSafely("check", this, entity, context)
        return result as? Boolean
            ?: throw IllegalStateException(
                "condition $id's handler returned a non-Boolean value: $result"
            )
    }

}
