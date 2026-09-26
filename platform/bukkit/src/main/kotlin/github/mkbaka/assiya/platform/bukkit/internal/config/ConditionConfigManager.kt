package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.api.condition.Condition
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigCondition
import github.mkbaka.assiya.platform.bukkit.internal.registry.ConditionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry

import github.mkbaka.bukkitksp.annotation.CustomTask

object ConditionConfigManager : ConfigSectionManager<Condition>("condition") {

    override fun defaultResources(): List<String> {
        return listOf("condition/level.yml")
    }

    @CustomTask(taskId = "reload", priority = 20)
    override fun reload() {
        for (condition in values) {
            ConditionRegistry.unregister(condition)
        }
        super.reload()
    }

    override fun convert(id: String, node: ConfigNode): Condition {
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

        val condition = ConfigCondition(id = id, names = names, reader = readerName, handler = handler)

        ConditionRegistry.register(condition)

        return condition
    }

}
