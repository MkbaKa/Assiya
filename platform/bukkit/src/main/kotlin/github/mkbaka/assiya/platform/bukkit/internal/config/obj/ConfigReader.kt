package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.parser.rule.TextPatternTemplate
import github.mkbaka.assiya.platform.bukkit.api.reader.Placeholder
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader


class ConfigReader(
    override val id: String,
    override val capture: Capture<Any>,
    override val patterns: List<TextPatternTemplate>,
    override val placeholders: Map<String, Placeholder>,
) : Reader
