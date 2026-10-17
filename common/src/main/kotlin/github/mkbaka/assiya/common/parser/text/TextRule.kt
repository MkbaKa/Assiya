package github.mkbaka.assiya.common.parser.text

import github.mkbaka.assiya.common.base.Capture

class TextRule(
    val names: List<String>,
    capture: Capture<*>,
    patterns: List<TextPatternTemplate>,
    val target: Target
) {

    val compiled: List<TextPatternTemplate.Compiled> = patterns.map { it.compile(names, capture) }

    sealed interface Target {

        data class Attribute(
            val id: String,
            val capture: Capture<Any>
        ) : Target

        data class Condition(
            val id: String
        ) : Target

    }

}