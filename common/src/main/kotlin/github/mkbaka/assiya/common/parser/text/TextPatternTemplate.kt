package github.mkbaka.assiya.common.parser.text

import github.mkbaka.assiya.common.base.Capture

/**
 * 基于占位符的正则模板
 */
class TextPatternTemplate(
    private val template: String
) {

    private val captureIds = CAPTURE.findAll(template)
        .map { it.groupValues[1] }
        .toList()

    fun compile(names: List<String>, capture: Capture<*>): Compiled {
        // 拼出属性名对应的正则 "物理伤害|攻击力|attack"
        val pattern = names.joinToString("|") { Regex.escape(it) }

        var regex = template.replace("{name}", "(?:$pattern)")
        val captures = linkedMapOf<String, Capture.Entry<*>>()

        // 把 caputre id 换成属性值的匹配规则
        for (id in captureIds) {
            val entry = capture[id] ?: error("Unknown capture id: $id")
            regex = regex.replace("<$id>", "(?<$id>${entry.value.regex})")
            captures[id] = entry
        }

        return Compiled(
            regex = Regex(regex),
            captures = captures
        )
    }

    data class Compiled(
        val regex: Regex,
        val captures: Map<String, Capture.Entry<*>>
    )

    companion object {

        val CAPTURE = Regex("<([A-Za-z][A-Za-z0-9]*)>")

    }

}
