package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Value
import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.operation.Operation
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate

class ReaderBuilder {

    private val captures = linkedMapOf<String, Capture.Entry<*>>()
    private val patterns = arrayListOf<TextPatternTemplate>()
    private val placeholders = linkedMapOf<String, Placeholder>()

    fun <T : Any> capture(name: String, operation: Operation<T>, value: Value<T>): ReaderBuilder {
        captures[name] = Capture.Entry(operation, value)
        return this
    }

    fun capture(name: String, operation: NumberOperations): ReaderBuilder {
        return capture(name, operation, Value.Number)
    }

    fun pattern(template: String): ReaderBuilder {
        patterns += TextPatternTemplate(template)
        return this
    }

    fun placeholder(name: String, calculator: Placeholder): ReaderBuilder {
        placeholders[name] = calculator
        return this
    }

    internal fun buildCaptures(): Capture<Any> = Capture.of(captures.toMap())

    internal fun buildPatterns(): List<TextPatternTemplate> = patterns.toList()

    internal fun buildPlaceholders(): Map<String, Placeholder> = placeholders.toMap()

}
