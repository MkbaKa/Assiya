package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.internal.damage.DamageMetaStack
import github.mkbaka.assiya.platform.bukkit.internal.metadata.MetadataRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistry
import java.util.*

object MythicPlaceholderValues {

    fun attribute(uuid: UUID?, args: String): String {
        val parts = args.split(":")
        val attributeId = parts[0]
        val captureId = parts.getOrNull(1)?.ifEmpty { "total" } ?: "total"
        val digits = parts.getOrNull(2)?.toInt() ?: 2

        val attribute = AttributeRegistry[attributeId] ?: error("Unknown attribute: $attributeId")
        val reader = attribute[ReaderComponent]?.reader ?: error("$attributeId has no reader")

        require(captureId in reader.capture || captureId in reader.placeholders) {
            "Unknown attribute value: $attributeId:$captureId"
        }

        val status = uuid?.let { AssiyaAPI.getSession(it)?.snapshot?.get(attributeId) } ?: Status.empty<Any>()
        val value = reader.read(status, captureId) ?: return "0"

        return if (value is Number) {
            String.format(Locale.ROOT, "%.${digits}f", value.toDouble())
        } else {
            value.toString()
        }
    }

    fun damageData(uuid: UUID?, args: String): String {
        val key = args.substringBefore("|")
        val default = args.substringAfter("|", "null")
        val task = uuid?.let { DamageMetaStack.getCurrent(it) } ?: return default
        return task.processor[CustomDataComponent]?.get(key)?.toString() ?: default
    }

    fun metadata(uuid: UUID?, args: String): String {
        val key = args.substringBefore("|")
        val default = args.substringAfter("|", "null")
        return uuid?.let { MetadataRegistry.getMetadata<Any>(it, key) }?.toString() ?: default
    }

}
