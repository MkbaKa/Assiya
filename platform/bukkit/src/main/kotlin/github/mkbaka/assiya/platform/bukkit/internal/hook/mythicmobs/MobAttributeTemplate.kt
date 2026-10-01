package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs

import github.mkbaka.assiya.platform.bukkit.internal.formula.Formula
import github.mkbaka.assiya.platform.bukkit.internal.formula.FormulaCompiler
import java.math.BigDecimal

internal class MobAttributeTemplate(mobId: String, attributes: List<String>, calculator: Boolean) {

    private val lines = arrayListOf<List<Part>>()

    init {
        for ((index, text) in attributes.withIndex()) {
            lines += if (calculator) compileLine("$mobId.attributes[$index]", text)
            else listOf(Part.Text(text))
        }
    }

    fun render(level: Double): List<String> {
        val result = ArrayList<String>(lines.size)
        for (line in lines) {
            result += buildString {
                for (part in line) {
                    when (part) {
                        is Part.Text -> append(part.value)
                        is Part.Expression -> {
                            val value = part.formula.invoke { name ->
                                require(name == "level") {
                                    "Formula ${part.formula.id} has unknown variable $name"
                                }
                                level
                            }
                            require(value.isFinite()) {
                                "Formula ${part.formula.id} produced a non-finite value"
                            }
                            append(BigDecimal.valueOf(value).stripTrailingZeros().toPlainString())
                        }
                    }
                }
            }
        }
        return result
    }

    private fun compileLine(id: String, text: String): List<Part> {
        val parts = arrayListOf<Part>()
        var offset = 0
        while (offset < text.length) {
            val start = text.indexOf("\${", offset)
            if (start < 0) {
                parts += Part.Text(text.substring(offset))
                break
            }
            if (start > offset) parts += Part.Text(text.substring(offset, start))
            val end = text.indexOf('}', start + 2)
            require(end >= 0) { "$id has an unclosed formula: $text" }
            parts += Part.Expression(
                FormulaCompiler.compile("$id@$start", text.substring(start + 2, end))
            )
            offset = end + 1
        }
        return parts
    }

    private sealed interface Part {
        data class Text(val value: String) : Part
        data class Expression(val formula: Formula) : Part
    }
}
