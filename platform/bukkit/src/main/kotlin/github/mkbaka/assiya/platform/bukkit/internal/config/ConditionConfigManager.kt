package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigCondition
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry

import github.mkbaka.bukkitksp.annotation.CustomTask

object ConditionConfigManager {

    private val conditions = hashMapOf<String, Condition>()

    val size: Int get() = conditions.size

    private fun defaultResources(): List<String> {
        return listOf("condition/example.yml")
    }

    @CustomTask(taskId = "reload", priority = 20)
    fun reload() {
        for (condition in conditions.values) {
            ConditionRegistry.unregister(condition)
        }
        conditions.clear()
        ConfigLoader.load("condition", defaultResources()) { id, root ->
            val node = root.getNode(id) ?: error("Condition $id must be a configuration section")
            val condition = parseCondition(id, node)
            ConditionRegistry.register(condition)
            conditions[id] = condition
        }
    }

    private fun parseCondition(id: String, node: ConfigNode): Condition {
        val names = node.getStrings("names")

        val readerName = node.getString("reader")
            ?: throw IllegalArgumentException("Condition $id is missing a reader")

        val reader = ReaderRegistry[readerName]
            ?: throw IllegalArgumentException("Could not find reader $readerName for condition $id")

        val handler = node.getString("handler")
            ?: throw IllegalArgumentException("Condition $id is missing a handler")

        if (!ScriptManager.containsScript(handler)) {
            throw IllegalArgumentException(
                "Handler $handler for condition $id is missing"
            )
        }

        return ConfigCondition(id = id, names = names, reader = readerName, handler = handler)
    }

}
