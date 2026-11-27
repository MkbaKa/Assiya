package org.serverct.ersha.attribute

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.WrappedProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.colored
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.replaceVariables
import org.bukkit.entity.Entity
import org.serverct.ersha.attribute.data.AttributeData
import java.util.*

class AttributeHandle(processor: BukkitProcessor) : WrappedProcessor(processor) {

    val attributeData: Map<UUID, AttributeData>
        get() {
            val data = linkedMapOf<UUID, AttributeData>()
            attackerOrKiller?.let { data[it.uniqueId] = AttributeData(it) }
            entity?.let { data[it.uniqueId] = AttributeData(it) }
            return data
        }

    val tempValue: MutableMap<String, Number> = TriggerValues(
        requireNotNull(processor[CustomDataComponent]) { "Processor has no custom data component" }
    )

    var damageIntensity: Double
        get() = (processor[CustomDataComponent]?.get("attributeplus:damage-intensity") as? Number)?.toDouble()
            ?: TODO("Unsupported method: damageIntensity")
        set(value) { processor[CustomDataComponent]?.set("attributeplus:damage-intensity", value) }

    var replaceAttackMessage: Boolean
        get() = processor[CustomDataComponent]?.get("attributeplus:replace-attack-message") == true
        set(value) { processor[CustomDataComponent]?.set("attributeplus:replace-attack-message", value) }

    fun updateTempValue(init: Boolean, attribute: String, value: Number): Number {
        tempValue[attribute] = if (init) value else getAttrTriggerValue(attribute) + value.toDouble()
        return value
    }

    @JvmOverloads
    fun updateTempAttributeValue(target: Entity, attribute: String, value: Number, setState: Boolean = false): Number {
        require(target.uniqueId == attackerOrKiller?.uniqueId || target.uniqueId == entity?.uniqueId) {
            "Entity is not part of this processor"
        }
        val id = AssiyaAPI.getAttribute(attribute)?.id ?: attribute
        val key = "attributeplus:temporary:${target.uniqueId}:$id"
        val data = processor[CustomDataComponent] ?: return value
        val next = if (setState) value.toDouble() else
            (data[key] as? Number)?.toDouble()?.plus(value.toDouble()) ?: value.toDouble()
        data[key] = next
        processor[CancellableAttributeComponent]?.setCancelled("${target.uniqueId}:$id", next == -1.0)
        return value
    }

    fun getTempAttributeValue(target: Entity, attribute: String): Double {
        val id = AssiyaAPI.getAttribute(attribute)?.id ?: attribute
        return (processor[CustomDataComponent]?.get("attributeplus:temporary:${target.uniqueId}:$id") as? Number)
            ?.toDouble() ?: 0.0
    }

    fun containsTempAttributeKey(target: Entity, attribute: String): Boolean {
        val id = AssiyaAPI.getAttribute(attribute)?.id ?: attribute
        return processor[CustomDataComponent]?.containsKey("attributeplus:temporary:${target.uniqueId}:$id") == true
    }

    fun getDamage(target: Entity): Double {
        if (target.uniqueId == attackerOrKiller?.uniqueId) {
            return getDamage()
        }
        require(target.uniqueId == entity?.uniqueId) { "Entity is not part of this processor" }
        return (processor[CustomDataComponent]?.get("attributeplus:target-damage") as? Number)?.toDouble() ?: 0.0
    }

    fun setDamage(target: Entity, value: Double) {
        if (value.isNaN()) return
        if (target.uniqueId == attackerOrKiller?.uniqueId) {
            setDamage(value)
        } else {
            require(target.uniqueId == entity?.uniqueId) { "Entity is not part of this processor" }
            processor[CustomDataComponent]?.set("attributeplus:target-damage", value)
        }
    }

    fun updateDamage(target: Entity, value: Double) {
        if (value.isNaN()) return
        if (target.uniqueId == attackerOrKiller?.uniqueId) {
            updateDamage(value)
        } else {
            setDamage(target, (getDamage(target) + value).coerceAtLeast(0.0))
        }
    }

    fun conversion(text: String): String {
        val variables = hashMapOf<String, String>()
        for ((key, value) in tempValue) {
            variables[key] = String.format(Locale.ROOT, "%.2f", value.toDouble())
        }
        variables["entityA"] = attackerOrKiller?.name.orEmpty()
        variables["entityB"] = entity?.name.orEmpty()
        for (match in Regex("\\{([^{}]+)}").findAll(text)) {
            variables.putIfAbsent(match.groupValues[1], "0")
        }
        return text.replaceVariables(variables).colored()
    }

    fun isAttrTriggerFormServer(name: String): Boolean = isAttrTrigger(placeholder(name))

    fun isAttrTriggerFormDefault(name: String): Boolean = isAttrTriggerFormServer(name)

    fun getAttrTriggerValueFromServer(name: String): Double = getAttrTriggerValue(placeholder(name))

    fun getAttrTriggerValueFromDefault(name: String): Double = getAttrTriggerValueFromServer(name)

    private fun placeholder(name: String): String {
        val attribute = AssiyaAPI.getAttribute(name) ?: return name
        return attribute[ApAttributeOptionsComponent]
            ?.get("placeholder") as? String ?: attribute.id
    }

    fun getAttrTriggerValue(key: String): Double {
        return tempValue[key]?.toDouble() ?: 0.0
    }

    fun isAttrTrigger(key: String): Boolean = tempValue.containsKey(key)

}

/** 在现有 CustomDataComponent 上提供可写数值视图, 不复制状态. */
private class TriggerValues(private val data: MutableMap<String, Any?>) : AbstractMutableMap<String, Number>() {
    private val prefix = "attributeplus:trigger-value:"

    override fun get(key: String): Number? = data[prefix + key] as? Number

    override fun put(key: String, value: Number): Number? = data.put(prefix + key, value) as? Number

    override fun remove(key: String): Number? = data.remove(prefix + key) as? Number

    override val entries: MutableSet<MutableMap.MutableEntry<String, Number>>
        get() = object : AbstractMutableSet<MutableMap.MutableEntry<String, Number>>() {
            override val size: Int get() = data.count { it.key.startsWith(prefix) && it.value is Number }

            override fun add(element: MutableMap.MutableEntry<String, Number>): Boolean =
                throw UnsupportedOperationException("Use put to store trigger values")

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, Number>> {
                val keys = data.filter { it.key.startsWith(prefix) && it.value is Number }.keys.toList().iterator()
                var current: String? = null
                return object : MutableIterator<MutableMap.MutableEntry<String, Number>> {
                    override fun hasNext(): Boolean = keys.hasNext()

                    override fun next(): MutableMap.MutableEntry<String, Number> {
                        val storedKey = keys.next()
                        current = storedKey
                        return object : MutableMap.MutableEntry<String, Number> {
                            override val key: String = storedKey.removePrefix(prefix)
                            override val value: Number get() = data.getValue(storedKey) as Number
                            override fun setValue(newValue: Number): Number =
                                data.put(storedKey, newValue) as Number

                            override fun equals(other: Any?): Boolean =
                                other is Map.Entry<*, *> && key == other.key && value == other.value

                            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
                        }
                    }

                    override fun remove() {
                        val storedKey = checkNotNull(current) { "Call next before remove" }
                        data.remove(storedKey)
                        current = null
                    }
                }
            }
        }
}
