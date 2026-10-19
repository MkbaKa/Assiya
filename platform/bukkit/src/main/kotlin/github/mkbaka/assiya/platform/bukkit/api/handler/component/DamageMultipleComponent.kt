package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent

class DamageMultipleComponent(
    private val components: MutableMap<String, DamageComponent> = hashMapOf()
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    val totalDamage: Double
        get() = components.values.sumOf { it.value }.coerceAtLeast(0.0)

    fun getDamage(id: String) = components[id]?.value

    fun clear() {
        components.clear()
    }

    fun setDamage(id: String, value: Double) {
        components.computeIfAbsent(id) { DamageComponent(id, 0.0) }.value = value
    }

    fun addDamage(id: String, value: Double) {
        val target = (getDamage(id) ?: 0.0) + value
        setDamage(id, target)
    }

    fun takeDamage(id: String, value: Double) {
        val target = (getDamage(id) ?: 0.0) - value
        setDamage(id, target.coerceAtLeast(0.0))
    }

    operator fun get(id: String) = components[id]

    companion object Key : ProcessorComponent.Key<DamageMultipleComponent>

}
