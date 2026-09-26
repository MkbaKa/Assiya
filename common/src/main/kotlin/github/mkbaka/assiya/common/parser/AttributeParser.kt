package github.mkbaka.assiya.common.parser

import github.mkbaka.assiya.common.parser.part.AttributeSourcePart

/**
 * 将待解析的片段解析成结果
 */
fun interface AttributeParser {

    fun parse(parts: List<AttributeSourcePart>): AttributeParsedSource

}