package github.mkbaka.assiya.common.engine.state

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.source.AttributeSourceInfo

/**
 * 属性总状态
 *      可能有各种 "源" 贡献了各种各样的值
 */
class AttributeState<T : Any>(
    private val capture: Capture<T>
) {

    private val contributors = linkedMapOf<String, Contributor<T>>()

    fun put(info: AttributeSourceInfo, status: Status<T>) {
        contributors[info.sourceId] = Contributor(
            priority = info.priority,
            sequence = info.sequence,
            status = status
        )
    }

    operator fun contains(info: AttributeSourceInfo): Boolean = contributors.containsKey(info.sourceId)

    fun sourceIds(): List<String> = contributors.keys.toList()

    fun remove(sourceId: String) = contributors.remove(sourceId) != null

    fun isEmpty() = contributors.isEmpty()

    fun copy(): AttributeState<T> {
        return AttributeState(capture).also { result ->
            result.contributors.putAll(contributors)
        }
    }

    fun snapshot(): Status<T> {
        return snapshot(null)
    }

    fun snapshot(additional: AttributeState<T>?): Status<T> {
        val values = ArrayList<Contributor<T>>(
            contributors.size + (additional?.contributors?.size ?: 0)
        )

        values.addAll(contributors.values)

        if (additional != null) {
            values.addAll(additional.contributors.values)
        }

        values.sortWith(
            compareBy<Contributor<T>> { it.priority }
                .thenBy { it.sequence }
        )

        return capture.merge(values.map { it.status })
    }

    private class Contributor<T : Any>(
        val priority: Int,
        val sequence: Long,
        val status: Status<T>
    )

}
