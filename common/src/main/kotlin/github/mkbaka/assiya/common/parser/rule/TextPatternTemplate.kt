package github.mkbaka.assiya.common.parser.rule

import github.mkbaka.assiya.common.base.Capture

/**
 * 基于占位符的正则模板
 */
class TextPatternTemplate(
    private val template: String
) {

    fun compile(names: List<String>, capture: Capture<*>): Compiled {
        // 取模板里的 capture id
        val captureIds = CAPTURE.findAll(template)
            .map { it.groupValues[1] }
            .toList()

        // 拼出属性名对应的正则 "物理伤害|攻击力|attack"
        val pattern = names.joinToString("|") { Regex.escape(it) }

        var regex = template.replace("{name}", "(?:$pattern)")
        for (id in captureIds) {
            val captureRegex = capture[id]?.value?.regex ?: error("Unknown capture id: $id")
            regex = regex.replace("<$id>", "(?<$id>$captureRegex)")
        }

        return Compiled(
            regex = Regex(regex),
            captures = captureIds.associateWith { capture[it]!! }
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
