package github.mkbaka.assiya.common.base

import github.mkbaka.assiya.common.operation.Operation

/**
 * 读取方式与其合并行为
 */
class Capture<T : Any>(
    private val operations: Map<String, Entry<T>>
) {

    operator fun get(capture: String) = operations[capture]

    operator fun contains(capture: String) = operations.containsKey(capture)

    fun merge(vararg incoming: Status<T>): Status<T> {
        return merge(incoming.asIterable())
    }

    fun merge(incoming: Iterable<Status<T>>): Status<T> {
        val iterator = incoming.iterator()
        // 为空代表没有需要合并的内容
        if (!iterator.hasNext()) {
            return Status.empty()
        }

        val merged = linkedMapOf<String, T>()
        for (status in incoming) {
            for ((captureId, value) in status.entries) {
                val operation = operations[captureId]?.operation

                // 若自身没有 capture 需要的 operation 则将其值直接存入
                // 如果后续的 status 仍然持有不存在的 operation 会覆盖上个 status 的值
                merged[captureId] = operation?.merge(merged[captureId], value) ?: value
            }
        }
        return Status.of(merged)
    }

    data class Entry<T : Any>(
        val operation: Operation<T>,
        val value: Value<T>
    )

    companion object{

        @Suppress("UNCHECKED_CAST")
        fun of(entries: Map<String, Entry<*>>): Capture<Any> {
            return Capture(entries as Map<String, Entry<Any>>)
        }

    }

}