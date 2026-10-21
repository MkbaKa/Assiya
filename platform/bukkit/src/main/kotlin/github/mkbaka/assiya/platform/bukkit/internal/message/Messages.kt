package github.mkbaka.assiya.platform.bukkit.internal.message

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.message.template.Message
import github.mkbaka.assiya.platform.bukkit.internal.message.template.impl.JsonMessage
import github.mkbaka.assiya.platform.bukkit.internal.message.template.impl.NormalMessage
import java.io.File
import java.util.*

class Messages(node: ConfigNode) {

    private val messages = buildMap {
        for (key in node.keys()) {
            val section = node.getNode(key)
            put(
                key,
                if (section?.getString("type") == "json") {
                    JsonMessage(section)
                } else {
                    NormalMessage(node.getStrings(key))
                }
            )
        }
    }

    operator fun get(key: String): Message = requireNotNull(messages[key]) { "Missing message: $key" }

    companion object {

        fun load(): Messages {
            val plugin = AssiyaBukkitPlugin.INSTANCE
            val localizedPath = "lang/${Locale.getDefault()}.yml"
            val available = File(plugin.dataFolder, localizedPath).isFile ||
                plugin.getResource(localizedPath)?.use { true } == true
            val path = if (available) localizedPath else "lang/en_US.yml"
            return Messages(LanguageFile.load(path))
        }

    }

}

