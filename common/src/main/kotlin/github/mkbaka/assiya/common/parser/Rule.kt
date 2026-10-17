package github.mkbaka.assiya.common.parser

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate

class Rule(
    val id: String,
    val capture: Capture<Any>,
    val names: List<String>,
    val patterns: List<TextPatternTemplate>
)
