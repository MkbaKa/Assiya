package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate
import java.util.function.Consumer

class SimpleReader(
    override val id: String,
    override val capture: Capture<Any>,
    patterns: List<TextPatternTemplate>,
    placeholders: Map<String, Placeholder> = emptyMap()
) : Reader {

    override val patterns = patterns.toList()
    override val placeholders = placeholders.toMap()

    private constructor(id: String, builder: ReaderBuilder) : this(
        id, builder.buildCaptures(), builder.buildPatterns(), builder.buildPlaceholders()
    )

    constructor(id: String, builder: Consumer<ReaderBuilder>) : this(
        id, ReaderBuilder().apply(builder::accept)
    )

}
