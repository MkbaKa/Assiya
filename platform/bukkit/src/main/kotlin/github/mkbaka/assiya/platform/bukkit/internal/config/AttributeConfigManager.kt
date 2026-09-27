package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerSettings
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigAttribute
import github.mkbaka.assiya.platform.bukkit.internal.registry.AttributeRegistration
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.TriggerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.bukkitksp.annotation.CustomTask

object AttributeConfigManager {

    private val attributes = hashMapOf<String, Attribute>()

    val size: Int get() = attributes.size

    private fun defaultResources(): List<String> {
        return listOf(
            "attribute/example.yml",
            "attribute/misc.yml",
            "attribute/fight/attack.yml",
            "attribute/fight/crit.yml",
            "attribute/fight/lightning.yml",
            "attribute/fight/vampire.yml",
            "attribute/fight/armor.yml",
            "attribute/fight/dodge.yml",
        )
    }

    @CustomTask(taskId = "reload", priority = 30)
    fun reload() {
        AttributeRegistration.batch {
            for (attribute in attributes.values) {
                attribute.unregister()
            }
            attributes.clear()
            ConfigLoader.load("attribute", defaultResources()) { id, root ->
                val node = root.getNode(id) ?: error("Attribute $id must be a configuration section")
                val attribute = parseAttribute(id, node)
                attribute.register()
                attributes[id] = attribute
            }
        }
    }

    private fun parseAttribute(id: String, value: ConfigNode): Attribute {
        val readerName = value.getString("reader")
        val reader = if (readerName == null) {
            null
        } else {
            requireNotNull(ReaderRegistry[readerName]) {
                "Could not find reader $readerName for attribute $id"
            }
            ReaderComponent(
                readerId = readerName,
                names = value.getStrings("names").ifEmpty { listOf(id) },
            )
        }

        val triggers = readTriggers(id, value)

        val options = value.getNode("options")?.let(::OptionsComponent)

        return ConfigAttribute(id, reader, triggers, options)
    }

    private fun readTriggerNodes(id: String, obj: Any?): List<Pair<String, ConfigNode?>> {
        val result = arrayListOf<Pair<String, ConfigNode?>>()

        when (obj) {
            /**
             * triggers: attack
             */
            is String -> {
                result += obj to null
            }

            /**
             * triggers: [attack, kill]
             * triggers:
             * - attack
             * - kill
             */
            is List<*> -> {
                for (ele in obj) {
                    result += ele.toString() to null
                }
            }

            /**
             * triggers:
             *   attack:
             *     priority: 10
             *     always-run: false
             *     handler: xxx
             *   defense:
             *     priority: 1000
             *     always-run: true
             *     handler: xxx
             */
            else -> {
                val node = ConfigNode.of(obj) ?: return result
                for (key in node.keys()) {
                    result += key to node.getNode(key)
                }
            }
        }

        return result
    }

    private fun readTriggers(id: String, node: ConfigNode): Map<Trigger, ConfigAttribute.ConfigTrigger> {
        val root = TriggerSettings.root(node)
        val result = hashMapOf<Trigger, ConfigAttribute.ConfigTrigger>()

        for ((name, childNode) in readTriggerNodes(id, node.get("triggers"))) {
            val type = TriggerRegistry[name] ?: error("Could not find trigger $name for attribute $id")
            val settings = root.child(childNode)
            val handler = settings.handler
                ?: error("Attribute $id has trigger ${type.trigger.id} but no handler")
            require(ScriptManager.containsScript(handler)) {
                "Handler $handler for attribute $id is missing"
            }

            try {
                type.validator.accept(settings)
                result[type.trigger] = ConfigAttribute.ConfigTrigger(settings)
            } catch (e: Exception) {
                logEx(e, "Failed to read ${type.trigger.id} settings for attribute $id")
            }
        }

        return result
    }

}
