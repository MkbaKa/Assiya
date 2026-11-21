package github.mkbaka.assiya.platform.bukkit.api.vanilla

import github.mkbaka.assiya.platform.bukkit.internal.vanilla.VanillaModifierCompatibility
import github.mkbaka.assiya.platform.bukkit.internal.vanilla.VanillaTransientRegistry
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity

class VanillaAttributeView internal constructor(
    val entity: LivingEntity,
    val attribute: Attribute
) {
    private val instance: AttributeInstance
        get() = requireNotNull(entity.getAttribute(attribute)) {
            "Entity ${entity.uniqueId} does not have attribute $attribute"
        }

    var base: Double
        get() = instance.baseValue
        set(value) { instance.baseValue = value }

    val value: Double get() = instance.value

    fun setTransient(source: String, amount: Double, operation: AttributeModifier.Operation) {
        set(source, amount, operation, true)
    }

    fun setPermanent(source: String, amount: Double, operation: AttributeModifier.Operation) {
        set(source, amount, operation, false)
    }

    fun addTransient(source: String, amount: Double, operation: AttributeModifier.Operation) {
        add(source, amount, operation, true)
    }

    fun addPermanent(source: String, amount: Double, operation: AttributeModifier.Operation) {
        add(source, amount, operation, false)
    }

    fun getModifier(source: String): AttributeModifier? {
        val identity = VanillaModifierCompatibility.identity(source)
        return VanillaModifierCompatibility.getModifier(instance, identity)
    }

    fun removeModifier(source: String): Boolean {
        val identity = VanillaModifierCompatibility.identity(source)
        val current = instance
        val modifier = VanillaModifierCompatibility.getModifier(current, identity)
            ?: return false
        current.removeModifier(modifier)
        VanillaTransientRegistry.forget(entity, attribute, modifier)
        return true
    }

    private fun set(source: String, amount: Double, operation: AttributeModifier.Operation, transient: Boolean) {
        // 先校验并构造新 modifier 再移除原有贡献.
        val modifier = VanillaModifierCompatibility.create(source, amount, operation)
        removeModifier(source)
        attach(modifier, transient)
    }

    private fun add(source: String, amount: Double, operation: AttributeModifier.Operation, transient: Boolean) {
        val modifier = VanillaModifierCompatibility.create(source, amount, operation)
        require(getModifier(source) == null) { "Attribute $attribute already has modifier source $source" }
        attach(modifier, transient)
    }

    private fun attach(modifier: AttributeModifier, transient: Boolean) {
        val current = instance
        if (transient && VanillaModifierCompatibility.addTransient(current, modifier)) return
        current.addModifier(modifier)
        if (transient) VanillaTransientRegistry.track(entity, attribute, current, modifier)
    }
}
