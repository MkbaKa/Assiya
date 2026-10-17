package github.mkbaka.assiya.platform.bukkit.internal.parser

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.parser.RuleSet
import github.mkbaka.assiya.common.parser.part.*
import github.mkbaka.assiya.common.parser.text.TextRuleMatched
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import java.time.Duration

object TextParserFactory : AttributeSourcePartParserFactory {

    override fun create(rules: RuleSet): AttributeSourcePartParser {
        val parser = TextAttributeSourcePartParser.compile(rules)
        return CachedTextParser(parser)
    }

    private class CachedTextParser(
        private val delegate: TextAttributeSourcePartParser
    ) : AttributeSourcePartParser {

        override val type: String = delegate.type

        private val cache: Cache<String, List<TextRuleMatched>> = Config.global.let { config ->
            Caffeine.newBuilder()
                .maximumSize(config.PARSER_CACHE_MAXIMUM_SIZE)
                .expireAfterAccess(Duration.ofSeconds(config.PARSER_CACHE_EXPIRE_AFTER_ACCESS_SECONDS))
                .build()
        }

        override fun parse(part: AttributeSourcePart): AttributeParsedSource {
            require(part is TextAttributeSourcePart) {
                "非文本类型无法由 ${this::class.java.simpleName} 解析"
            }
            val results = part.lines.map { line -> cache.get(line) { delegate.parseLine(line) }!! }
            return delegate.toSource(results)
        }

    }

}
