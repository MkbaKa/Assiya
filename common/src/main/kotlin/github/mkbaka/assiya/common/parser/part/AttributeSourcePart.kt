package github.mkbaka.assiya.common.parser.part

/**
 * 代表一个待解析的片段
 */
interface AttributeSourcePart {

    val type: String

    object Empty : AttributeSourcePart {
        override val type: String = "empty"
    }

}