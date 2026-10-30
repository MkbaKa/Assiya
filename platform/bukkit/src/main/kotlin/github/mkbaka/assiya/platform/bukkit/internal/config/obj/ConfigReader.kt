package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.formula.Formula
import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate
import github.mkbaka.assiya.platform.bukkit.api.reader.Placeholder
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader
import github.mkbaka.assiya.platform.bukkit.api.reader.ReaderEvaluation


class ConfigReader(
    override val id: String,
    override val capture: Capture<Any>,
    override val patterns: List<TextPatternTemplate>,
    /**
     * 公式内的变量只有已声明的数字 capture 才按 capture 读取
     */
    private val numeric: Set<String>,
    /**
     * placeholder 名到公式的对应关系, 决定哪些名字参与共享求值
     */
    private val formulas: Map<String, Formula>
) : Reader {

    override val placeholders: Map<String, Placeholder>

    init {
        val parsed = linkedMapOf<String, Placeholder>()
        for (name in formulas.keys) {
            parsed[name] = Placeholder { status -> evaluate(status).read(name) as? Double ?: 0.0 }
        }
        placeholders = parsed
    }

    override fun evaluate(status: Status<*>): ReaderEvaluation {
        return ConfigReaderEvaluation(status)
    }

    private inner class ConfigReaderEvaluation(
        status: Status<*>
    ) : ReaderEvaluation(this@ConfigReader, status) {

        override fun read(name: String): Any? {
            // 不是公式就直接返回结果
            val formula = formulas[name]
                ?: return this@ConfigReader.read(status, name)

            return resolve(name) {
                formula.invoke { variable ->
                    when (variable) {
                        in numeric -> {
                            val entry = capture[variable] ?: return@invoke null

                            var result = status[variable]

                            if (result == null) {
                                result = if (entry.operation == NumberOperations.Multiply) 1.0 else 0.0
                            }

                            result
                        }

                        in formulas -> read(variable)

                        else -> null
                    }
                }
            }
        }

    }

}
