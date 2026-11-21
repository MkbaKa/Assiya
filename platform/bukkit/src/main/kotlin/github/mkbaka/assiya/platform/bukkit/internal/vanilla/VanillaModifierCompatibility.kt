package github.mkbaka.assiya.platform.bukkit.internal.vanilla

import org.bukkit.NamespacedKey
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import java.util.*

internal object VanillaModifierCompatibility {

    private val SUPPORT_KEY_CONSTRUCTOR = AttributeModifier::class.java.constructors.any {
        it.parameterTypes.contentEquals(arrayOf(NamespacedKey::class.java, Double::class.javaPrimitiveType, AttributeModifier.Operation::class.java))
    }

    private val SUPPORT_KEY_GETTER = AttributeModifier::class.java.methods.any {
        it.name == "getKey" && it.parameterCount == 0 && it.returnType == NamespacedKey::class.java
    }

    private val SUPPORT_TRANSIENT_MODIFIER = AttributeInstance::class.java.methods.any {
        it.name == "addTransientModifier" && it.parameterTypes.contentEquals(arrayOf(AttributeModifier::class.java))
    }

    private val SUPPORT_KEY_LOOKUP = AttributeInstance::class.java.methods.any {
        it.name == "getModifier" && it.parameterTypes.map { type -> type.name } == listOf("net.kyori.adventure.key.Key")
    }

    private val SUPPORT_UUID_LOOKUP = AttributeInstance::class.java.methods.any {
        it.name == "getModifier" && it.parameterTypes.contentEquals(arrayOf(UUID::class.java))
    }

    data class Identity(val key: NamespacedKey, val uuid: UUID)

    fun identity(source: String): Identity {
        val separator = source.indexOf(':')
        return Identity(
            NamespacedKey(source.substring(0, separator), source.substring(separator + 1)),
            UUID.nameUUIDFromBytes(source.toByteArray(Charsets.UTF_8))
        )
    }

    @Suppress("DEPRECATION", "removal")
    fun create(source: String, amount: Double, operation: AttributeModifier.Operation): AttributeModifier {
        val identity = identity(source)
        return if (SUPPORT_KEY_CONSTRUCTOR) {
            AttributeModifier(identity.key, amount, operation)
        } else {
            AttributeModifier(identity.uuid, source, amount, operation)
        }
    }

    fun getModifier(instance: AttributeInstance, identity: Identity): AttributeModifier? {
        return if (SUPPORT_KEY_CONSTRUCTOR && SUPPORT_KEY_GETTER) {
            getModifierByKey(instance, identity.key)
        } else {
            getModifierByUuid(instance, identity.uuid)
        }
    }

    @Suppress("DEPRECATION", "removal")
    fun getModifier(instance: AttributeInstance, modifier: AttributeModifier): AttributeModifier? {
        return if (SUPPORT_KEY_GETTER) {
            getModifierByKey(instance, modifier.key)
        } else {
            getModifierByUuid(instance, modifier.uniqueId)
        }
    }

    private fun getModifierByKey(instance: AttributeInstance, key: NamespacedKey): AttributeModifier? {
        if (SUPPORT_KEY_LOOKUP) return instance.getModifier(key)
        // 低版本没有根据 key / uuid 获取 modifier 的方法
        return instance.modifiers.firstOrNull { it.key == key }
    }

    @Suppress("DEPRECATION", "removal")
    private fun getModifierByUuid(instance: AttributeInstance, uuid: UUID): AttributeModifier? {
        if (SUPPORT_UUID_LOOKUP) return instance.getModifier(uuid)
        return instance.modifiers.firstOrNull { it.uniqueId == uuid }
    }

    fun addTransient(instance: AttributeInstance, modifier: AttributeModifier): Boolean {
        if (!SUPPORT_TRANSIENT_MODIFIER) return false
        instance.addTransientModifier(modifier)
        return true
    }

}
