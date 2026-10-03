package github.mkbaka.assiya.platform.bukkit.internal.damage

import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import java.util.*

object DamageProcessorStack {

    private val stacks = hashMapOf<UUID, ArrayDeque<DamageProcessor>>()

    @JvmStatic
    fun getCurrent(uuid: UUID): DamageProcessor? = stacks[uuid]?.peekLast()

    fun push(uuid: UUID, processor: DamageProcessor) {
        stacks.computeIfAbsent(uuid) { ArrayDeque() }.addLast(processor)
    }

    fun pop(uuid: UUID) {
        val stack = stacks[uuid]!!
        stack.removeLast()
        if (stack.isEmpty()) {
            stacks.remove(uuid)
        }
    }

}
