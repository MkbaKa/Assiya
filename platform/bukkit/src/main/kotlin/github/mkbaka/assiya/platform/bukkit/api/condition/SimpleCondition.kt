package github.mkbaka.assiya.platform.bukkit.api.condition

import org.bukkit.entity.Entity
import java.util.function.BiPredicate
import java.util.function.Consumer

class SimpleCondition(
    override val id: String,
    override val components: Map<ConditionComponent.Key<*>, ConditionComponent>,
    private val predicate: BiPredicate<Entity, ConditionContextImpl>
) : Condition {

    constructor(
        id: String,
        builder: Consumer<ConditionComponentBuilder>,
        predicate: BiPredicate<Entity, ConditionContextImpl>
    ) : this(id, ConditionComponentBuilder().apply(builder::accept).build(), predicate)

    override fun check(entity: Entity, context: ConditionContextImpl): Boolean = predicate.test(entity, context)

}
