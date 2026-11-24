package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ProcessorComponent

class HealMultipleComponent(
    private val components: MutableMap<String, HealComponent> = hashMapOf()
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    val totalHeal: Double
        get() = components.values.sumOf { it.value }.coerceAtLeast(0.0)

    fun getHeal(id: String) = components[id]?.value

    fun clear() {
        components.clear()
    }

    fun setHeal(id: String, value: Double) {
        components.computeIfAbsent(id) { HealComponent(id, 0.0) }.value = value
    }

    fun addHeal(id: String, value: Double) {
        val target = (getHeal(id) ?: 0.0) + value
        setHeal(id, target)
    }

    fun takeHeal(id: String, value: Double) {
        val target = (getHeal(id) ?: 0.0) - value
        setHeal(id, target.coerceAtLeast(0.0))
    }

    operator fun get(id: String) = components[id]

    companion object Key : ProcessorComponent.Key<HealMultipleComponent>

}
