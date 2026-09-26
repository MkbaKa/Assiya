package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.parser.rule.TextPatternTemplate
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.api.reader.Placeholder
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader
import github.mkbaka.assiya.platform.bukkit.internal.config.obj.ConfigReader
import github.mkbaka.assiya.platform.bukkit.internal.formula.Formula
import github.mkbaka.assiya.platform.bukkit.internal.formula.FormulaCompiler
import github.mkbaka.assiya.platform.bukkit.internal.registry.OperationsRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.ValueRegistry
import github.mkbaka.bukkitksp.annotation.CustomTask

internal object ReaderConfigManager : ConfigSectionManager<Reader>(
    "reader"
) {

    override fun defaultResources(): List<String> {
        return listOf("reader/default.yml")
    }

    @CustomTask(taskId = "reload", priority = 10)
    override fun reload() {
        for (reader in values) {
            ReaderRegistry.unregister(reader)
        }
        super.reload()
    }

    override fun convert(id: String, node: ConfigNode): Reader {
        val section = node.getNode("captures")
            ?: error("Reader $id is missing captures")

        val captures = hashMapOf<String, Capture.Entry<*>>()

        for (key in section.keys()) {
            captures[key] = parseCapture(section, key)
        }

        val patterns = node.getStrings("patterns").map(::TextPatternTemplate)

        return ConfigReader(
            id = id,
            capture = Capture.of(captures),
            patterns = patterns,
            placeholders = parsePlaceholders(id, node.getNode("placeholders"), captures)
        ).also { it.register() }
    }

    private fun parsePlaceholders(
        id: String,
        node: ConfigNode?,
        captures: Map<String, Capture.Entry<*>>
    ): Map<String, Placeholder> {
        if (node == null) {
            return emptyMap()
        }

        val numeric = captures.filterValues { it.operation is NumberOperations }
        val formulas = hashMapOf<String, Formula>()

        for (key in node.keys()) {
            formulas[key] = FormulaCompiler.compile("${id}.placeholder.${key}", node.getString(key)!!)
        }

        // 公式内的变量解析
        fun evaluate(name: String, status: Status<*>, resolving: MutableSet<String>): Double {
            check(resolving.add(name)) { "Placeholder $name in reader $id contains a circular reference" }
            val formula = formulas[name]!!
            return try {
                formula.invoke { variable ->
                    val capture = numeric[variable]
                    if (capture != null) {
                        status[variable] ?: if (capture.operation == NumberOperations.Multiply) 1.0 else 0.0
                    } else if (variable in formulas) {
                        evaluate(variable, status, resolving)
                    } else {
                        null
                    }
                }
            } finally {
                resolving.remove(name)
            }
        }

        return formulas.mapValues { (name, _) -> Placeholder { status -> evaluate(name, status, hashSetOf()) } }
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
