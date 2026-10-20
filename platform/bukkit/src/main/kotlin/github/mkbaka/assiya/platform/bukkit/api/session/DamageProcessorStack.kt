package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import java.util.*

class DamageProcessorStack {


    private val stacks = ArrayDeque<DamageProcessor>()

    val current: DamageProcessor?
        get() = stacks.peekLast()

    fun push(processor: DamageProcessor) {
        stacks.addLast(processor)
    }

    fun pop() {
        stacks.removeLast()
    }

}
