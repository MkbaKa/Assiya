package github.mkbaka.assiya.platform.bukkit.api.condition

interface ConditionComponent {
    val key: Key<*>

    interface Key<C : ConditionComponent>
}
