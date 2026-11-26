package org.serverct.ersha.attribute

import github.mkbaka.assiya.common.formula.Formula
import github.mkbaka.assiya.common.formula.FormulaCompiler
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.WrappedAttribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.serverct.ersha.attribute.data.AttributeData
import org.serverct.ersha.manager.data.Data
import java.util.function.DoubleSupplier

class ApWrappedAttribute(attribute: Attribute, val attributeType: String) : WrappedAttribute(attribute) {

    private var formula: Formula? = null

    private fun attributeId(name: String): String = AssiyaAPI.getAttribute(name)?.id ?: name

    val placeholder: String get() = (attribute[ApAttributeOptionsComponent]?.get("placeholder") as? String) ?: id

    var priority: Int
        get() = (mutableOptions()["priority"] as? Number)?.toInt() ?: 0
        set(value) { mutableOptions()["priority"] = value }

    var combatPower: Double
        get() = (mutableOptions()["combat-power"] as? Number)?.toDouble() ?: 0.0
        set(value) { mutableOptions()["combat-power"] = value }

    var attributeValue: Double
        get() = (mutableOptions()["maximum"] as? Number)?.toDouble() ?: -1.0
        set(value) { mutableOptions()["maximum"] = value }

    fun setAsync(value: Boolean): Unit =
        throw UnsupportedOperationException("AP asynchronous attributes are not supported")

    fun setSyncUpdateState(value: Boolean): Unit =
        throw UnsupportedOperationException("AP sync-update is not supported")

    fun setSkipFilter(value: Boolean) {
        mutableOptions()["skip-filter"] = value
    }

    fun setIgnoreCancelled(value: Boolean) {
        mutableOptions()["ignore-cancelled"] = value
    }

    fun isIgnoreCancelled(): Boolean = mutableOptions()["ignore-cancelled"] == true

    fun setFormula(value: String) {
        val compiled = if (value.isEmpty()) null else FormulaCompiler.compile("attributeplus:$id", value, '{', '}')
        formula = compiled
        mutableOptions()["formula"] = value
    }

    fun setHologram(value: String): Unit =
        throw UnsupportedOperationException("AP holograms are not supported")

    fun getFormulaValue(processor: BukkitProcessor, defaultFormula: DoubleSupplier): Double {
        val compiled = formula ?: return defaultFormula.asDouble
        val handle = AttributeHandle(processor)
        return compiled.invoke { variable ->
            when (variable) {
                "attackerAttack" -> handle.attackerOrKiller?.let { getDamage(it, processor) } ?: 0.0
                "entityAttack" -> handle.entity?.let { getDamage(it, processor) } ?: 0.0
                "value" -> handle.attackerOrKiller?.let { getRandomValue(it, processor) } ?: 0.0
                else -> {
                    val parts = variable.split(':', limit = 2)
                    val entity = when (parts[0]) {
                        "entityA" -> handle.attackerOrKiller
                        "entityB" -> handle.entity
                        else -> null
                    }
                    if (entity != null && parts.size == 2) {
                        getRandomValue(entity, parts[1], processor).coerceAtLeast(0.0)
                    } else 0.0
                }
            }
        }
    }

    @JvmOverloads
    fun setMessages(messages: List<String>, replaceAttack: Boolean = false) {
        mutableOptions()["messages"] = messages
        mutableOptions()["replace-attack-message"] = replaceAttack
    }

    fun getAttributeValue(entity: Entity, processor: BukkitProcessor): Array<Number> =
        throw UnsupportedOperationException("Attribute ranges are not supported. Use getRandomValue instead.")

    fun getAttributeValue(entity: Entity, name: String, processor: BukkitProcessor): Array<Number> =
        throw UnsupportedOperationException("Attribute ranges are not supported. Use getRandomValue instead.")

    private fun mutableOptions(): MutableMap<String, Any?> =
        requireNotNull(attribute[ApAttributeOptionsComponent]) { "Attribute $id has no mutable AP options" }

    override fun getRandomValue(entity: Entity, attribute: String, processor: BukkitProcessor): Double {
        val attributeId = attributeId(attribute)
        val base = super.getRandomValue(entity, attributeId, processor)
        val value = base + AttributeHandle(processor).getTempAttributeValue(entity, attributeId)
        val attr = AttributeRegistry[attributeId]
        val maximum = (attr?.get(ApAttributeOptionsComponent)?.get("maximum") as? Number)?.toDouble() ?: -1.0
        val accepted = if (maximum == -1.0) value else value.coerceAtMost(maximum)
        val key = attr?.get(ApAttributeOptionsComponent)?.get("placeholder") as? String ?: attr?.id
        if (key != null) storageValue(key, accepted, processor)
        return accepted
    }

    fun getDamage(entity: Entity, processor: BukkitProcessor): Double {
        return AttributeHandle(processor).getDamage(entity)
    }

    fun setDamage(entity: Entity, value: Double, processor: BukkitProcessor) {
        AttributeHandle(processor).setDamage(entity, value)
    }

    fun addDamage(entity: Entity, value: Double, processor: BukkitProcessor) {
        AttributeHandle(processor).updateDamage(entity, value)
    }

    fun takeDamage(entity: Entity, value: Double, processor: BukkitProcessor) = addDamage(entity, -value, processor)

    fun addAttribute(entity: Entity, name: String, value: Double, processor: BukkitProcessor) {
        if (!value.isNaN()) AttributeHandle(processor).updateTempAttributeValue(entity, name, value)
    }

    fun takeAttribute(entity: Entity, name: String, value: Double, processor: BukkitProcessor) =
        addAttribute(entity, name, -value, processor)

    fun setAttribute(entity: Entity, name: String, value: Double, processor: BukkitProcessor) {
        if (!value.isNaN()) AttributeHandle(processor).updateTempAttributeValue(entity, name, value, true)
    }

    fun cancelAttribute(entity: Entity, name: String, processor: BukkitProcessor) {
        AttributeHandle(processor).updateTempAttributeValue(entity, name, -1.0, true)
    }

    fun getDamageIntensity(processor: BukkitProcessor): Double = AttributeHandle(processor).damageIntensity

    fun registerAttribute() = attribute.register()

    fun getCombatPower(value: Array<Number>): Double = value[1].toDouble() * combatPower

    fun toPlaceholder(name: String): String {
        val attribute = AssiyaAPI.getAttribute(name) ?: return name
        return attribute[ApAttributeOptionsComponent]?.get("placeholder") as? String ?: attribute.id
    }

    fun toServerName(name: String): String = AssiyaAPI.getAttribute(name)
        ?.get(ReaderComponent)?.names?.firstOrNull() ?: name

    fun toDefaultName(name: String): String = toServerName(name)

    fun storageValue(name: String, value: Double, processor: BukkitProcessor) = storageValue(name, value, false, processor)

    fun storageValue(name: String, value: Double, replace: Boolean, processor: BukkitProcessor) {
        AttributeHandle(processor).updateTempValue(replace, name, value)
    }

    fun storageValue(value: Pair<String, Double>, processor: BukkitProcessor) =
        storageValue(value.first, value.second, processor)

    fun storageValue(value: Pair<String, Double>, replace: Boolean, processor: BukkitProcessor) =
        storageValue(value.first, value.second, replace, processor)

    fun onTriggered(processor: BukkitProcessor) {
        val handle = AttributeHandle(processor)
        if (mutableOptions()["replace-attack-message"] == true) handle.replaceAttackMessage = true
        @Suppress("UNCHECKED_CAST")
        val messages = mutableOptions()["messages"] as? List<String> ?: return
        processor[CallbackComponent]?.callback(ignoreCancelled = true, runnable = Runnable {
            if (id == "attack" && (handle.replaceAttackMessage || handle.isCancelled())) return@Runnable
            processor[DamageMultipleComponent]?.let {
                handle.updateTempValue(true, "attackerDamage", it.totalDamage)
                val value = (processor[CustomDataComponent]?.get("attributeplus:target-damage") as? Number) ?: 0.0
                handle.updateTempValue(true, "entityDamage", value)
            }
            val attacker = handle.attackerOrKiller
            val target = handle.entity
            if (attacker is Player && messages.isNotEmpty() && messages[0] != "X") {
                ScriptTools.sendMessage(attacker, handle.conversion(messages[0]), emptyMap())
            }
            if (target is Player && messages.size > 1 && messages[1] != "X") {
                ScriptTools.sendMessage(target, handle.conversion(messages[1]), emptyMap())
            }
        })
    }

    fun getData(entity: Entity, processor: BukkitProcessor): AttributeData {
        require(container(entity, processor) != null) { "Entity is not part of this processor" }
        return AttributeData(entity)
    }

    fun storageValue(value: Data<String, Double>, processor: BukkitProcessor) =
        storageValue(value.key, value.value, processor)

    fun storageValue(value: Data<String, Double>, replace: Boolean, processor: BukkitProcessor) =
        storageValue(value.key, value.value, replace, processor)
}
