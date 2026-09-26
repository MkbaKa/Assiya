package github.mkbaka.assiya.common.parser

import github.mkbaka.assiya.common.parser.part.AttributeSourcePart
import github.mkbaka.assiya.common.parser.part.AttributeSourcePartParser

class DefaultAttributeParser(
    partParsers: List<AttributeSourcePartParser>
) : AttributeParser {

    private val parsers = partParsers.associateBy { it.type }

    override fun parse(parts: List<AttributeSourcePart>): AttributeParsedSource {
        if (parts.isEmpty()) return AttributeParsedSource.EMPTY

        val parseds = arrayListOf<AttributeParsedSource.Parsed>()

        for (part in parts) {
            val parser = parsers[part.type] ?: continue

            val parsed = parser.parse(part)

            parseds += parsed.parseds
        }

        return AttributeParsedSource(parseds)
    }

}