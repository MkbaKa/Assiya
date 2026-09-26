package github.mkbaka.assiya.common.parser.part

class TextAttributeSourcePart(
    val lines: List<String>
) : AttributeSourcePart {

    override val type: String = TYPE

    companion object {

        val TYPE = "text"

    }

}