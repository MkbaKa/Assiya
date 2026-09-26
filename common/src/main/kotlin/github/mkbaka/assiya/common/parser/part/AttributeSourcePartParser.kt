package github.mkbaka.assiya.common.parser.part

import github.mkbaka.assiya.common.parser.AttributeParsedSource

/**
 * 单个片段的解析实现
 */
interface AttributeSourcePartParser {

    val type: String

    fun parse(part: AttributeSourcePart): AttributeParsedSource

}