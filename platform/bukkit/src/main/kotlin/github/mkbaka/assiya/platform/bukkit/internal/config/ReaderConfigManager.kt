package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.formula.Formula
import github.mkbaka.assiya.common.formula.FormulaCompiler
import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigReader
import github.mkbaka.assiya.platform.bukkit.internal.registry.OperationsRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ValueRegistry
import github.mkbaka.bukkitksp.annotation.CustomTask

internal object ReaderConfigManager {

    private val readers = hashMapOf<String, Reader>()

    val size: Int get() = readers.size

    private fun defaultResources(): List<String> {
        return listOf(
            "reader/default.yml",
            "reader/percent.yml",
            "reader/text.yml"
        )
    }

    @CustomTask(taskId = "reload", priority = 10)
    fun reload() {
        for (reader in readers.values) {
            ReaderRegistry.unregister(reader)
        }
        readers.clear()
        ConfigLoader.load("reader", defaultResources()) { id, root ->
            val node = root.getNode(id) ?: error("Reader $id must be a configuration section")
            val reader = parseReader(id, node)
            ReaderRegistry.register(reader)
            readers[id] = reader
        }
    }

    private fun parseReader(id: String, node: ConfigNode): Reader {
        val section = node.getNode("captures")
            ?: error("Reader $id is missing captures")

        val captures = linkedMapOf<String, Capture.Entry<*>>()

        for (key in section.keys()) {
            captures[key] = parseCapture(section, key)
        }

        val patterns = node.getStrings("patterns").map(::TextPatternTemplate)

        val numeric = captures.filterValues { it.operation is NumberOperations }.keys.toHashSet()
        val formulas = parseFormulas(id, node.getNode("placeholders"))

        return ConfigReader(
            id = id,
            capture = Capture.of(captures),
            patterns = patterns,
            numeric = numeric,
            formulas = formulas
        )
    }

    private fun parseFormulas(id: String, node: ConfigNode?): Map<String, Formula> {
        if (node == null) {
            return emptyMap()
        }

        val formulas = linkedMapOf<String, Formula>()
        for (key in node.keys()) {
            formulas[key] = FormulaCompiler.compile("${id}.placeholder.${key}", node.getString(key)!!)
        }
        return formulas
    }

    private fun parseCapture(section: ConfigNode, capture: String): Capture.Entry<*> {
        val subNode = section.getNode(capture)
        // 简写 "value: add"
        if (subNode == null) {
            val op = section.getString(capture)
                ?: error("Capture $capture is missing an operation")
            return Capture.Entry(OperationsRegistry.of(op), ValueRegistry.of("decimal"))
        }

        /**
         * 完整节点
         * value:
         *   op: add
         *   format: decimal
         */
        val op = subNode.getString("op") ?: error("Capture $capture is missing an operation")
        val format = subNode.getString("format") ?: error("Capture $capture is missing a format")

        return Capture.Entry(OperationsRegistry.of(op), ValueRegistry.of(format))
    }

}
