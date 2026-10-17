package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry

interface Reader {

    val id: String

    val capture: Capture<Any>

    val patterns: List<TextPatternTemplate>

    val placeholders: Map<String, Placeholder>
        get() = emptyMap()

    fun read(status: Status<*>, captureId: String): Any? {
        return placeholders[captureId]?.resolve(status)
            ?: status[captureId]
    }

    fun register() {
        ReaderRegistry.register(this)
    }

    fun unregister() {
        ReaderRegistry.unregister(this)
    }

}

