package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.parser.text.TextPatternTemplate
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry

interface Reader {

    val id: String

    val capture: Capture<Any>

    val patterns: List<TextPatternTemplate>

    val placeholders: Map<String, Placeholder>
        get() = emptyMap()

    /**
     * 创建本次读取的求值对象
     * 见 [ReaderEvaluation] 的注释
     */
    fun evaluate(status: Status<*>): ReaderEvaluation = ReaderEvaluation(this, status)

    /**
     * 不固定公式结果的快速读取
     */
    fun read(status: Status<*>, captureId: String): Any? {
        return placeholders[captureId]?.resolve(status)
            ?: status[captureId]
    }

    fun defaultValue(captureId: String): Any? {
        val entry = capture[captureId] ?: return null
        return if (entry.operation == NumberOperations.Multiply) 1.0 else entry.value.defaultValue
    }

    fun register() {
        ReaderRegistry.register(this)
    }

    fun unregister() {
        ReaderRegistry.unregister(this)
    }

}
