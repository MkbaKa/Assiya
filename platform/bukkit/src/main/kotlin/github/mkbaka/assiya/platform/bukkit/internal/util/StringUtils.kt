package github.mkbaka.assiya.platform.bukkit.internal.util

import org.bukkit.ChatColor
import java.math.BigDecimal
import java.math.RoundingMode

object StringUtils {

    @JvmStatic
    fun Number.format(digits: Int): String = BigDecimal(toString()).setScale(digits, RoundingMode.HALF_UP).toString()

    @JvmStatic
    fun String.colored(): String {
        return ChatColor.translateAlternateColorCodes('&', this)
    }

    @JvmStatic
    fun String.uncolored(): String {
        return ChatColor.stripColor(this)!!
    }

    @JvmStatic
    fun List<String>.colored(): List<String> {
        return map { it.colored() }
    }

    @JvmStatic
    fun List<String>.uncolored(): List<String> {
        return map { it.uncolored() }
    }

    @JvmStatic
    @JvmOverloads
    fun String.replaceVariables(
        variables: Map<String, String>,
        prefix: Char = '{',
        suffix: Char = '}'
    ): String {
        if (this.isEmpty() || variables.isEmpty()) {
            return this
        }

        require(prefix != suffix) { "Prefix and suffix must be different characters" }

        val result = StringBuilder(this.length)
        val variable = StringBuilder()

        var insidePlaceholder = false

        for (ch in this) {
            if (insidePlaceholder) {
                if (ch == suffix) {
                    insidePlaceholder = false
                    val key = variable.toString()
                    variable.setLength(0)
                    val replacement = variables[key]
                    if (replacement == null) {
                        result.append(prefix).append(key).append(suffix)
                    } else {
                        result.append(replacement)
                    }
                } else {
                    variable.append(ch)
                }
                continue
            }

            if (ch == prefix) {
                insidePlaceholder = true
                variable.setLength(0)
                continue
            }

            result.append(ch)
        }

        if (insidePlaceholder) {
            result.append(prefix).append(variable)
        }

        return result.toString()
    }

}
