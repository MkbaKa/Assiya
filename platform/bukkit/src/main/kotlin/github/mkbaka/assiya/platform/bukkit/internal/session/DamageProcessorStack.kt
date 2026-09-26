package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import java.util.*

class DamageProcessorStack {

    // 当前执行中的所有伤害过程
    // key 为 source name
    private val processors = HashMap<String, Processing>()

    // 基于执行深度的索引
    private val processorIndexes = HashMap<Int, Processing>()

    // 当前执行中的 processor 索引
    private var current = -1

    fun getCurrent(): Processing? {
        return processorIndexes[current]
    }

    fun push(key: String = UUID.randomUUID().toString(), processor: DamageProcessor) {
        val processing = Processing(key, ++current, processor)
        processors[key] = processing
        processorIndexes[++current] = processing
    }

    fun pop() {
        getCurrent()?.let {
            processorIndexes.remove(current)
            processors.remove(it.source)
            current--
        }
    }

    data class Processing(
        val source: String,
        val index: Int,
        val processor: DamageProcessor
    )

}