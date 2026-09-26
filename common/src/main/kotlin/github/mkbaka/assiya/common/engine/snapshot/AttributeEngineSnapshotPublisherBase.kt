package github.mkbaka.assiya.common.engine.snapshot

import github.mkbaka.assiya.common.engine.state.AttributeState
import github.mkbaka.assiya.common.source.AttributeSourceInfo

import java.util.concurrent.atomic.AtomicReference


/**
 * 快照更新
 */
open class AttributeEngineSnapshotPublisherBase :
    AttributeEngineSnapshotPublisher {

    protected val snapshotRef = AtomicReference(AttributeEngineSnapshot.EMPTY)

    protected var raw = AttributeEngineSnapshot.EMPTY

    override fun current(): AttributeEngineSnapshot = snapshotRef.get()

    override fun publish(
        affectedAttributes: Set<String>,
        changedSources: Set<String>,
        sourceIndex: Map<String, AttributeSourceInfo>,
        attributeIndex: Map<String, AttributeState<*>>,
    ) {
        // 拿上一轮未发布前的快照做差异对比
        // 如果用发布后的 current() 结果可能包含外部操作后的数据
        val snapshot = buildSnapshot(
            oldSnapshot = raw,
            affectedAttributes = affectedAttributes,
            changedSources = changedSources,
            sourceIndex = sourceIndex,
            attributeIndex = attributeIndex,
        )
        raw = snapshot
        publish(snapshot)
    }

    open fun publish(snapshot: AttributeEngineSnapshot) {
        snapshotRef.set(snapshot)
    }

    private fun buildSnapshot(
        oldSnapshot: AttributeEngineSnapshot,
        affectedAttributes: Set<String>,
        changedSources: Set<String>,
        sourceIndex: Map<String, AttributeSourceInfo>,
        attributeIndex: Map<String, AttributeState<*>>,
    ): AttributeEngineSnapshot {
        val attributes = oldSnapshot.attributes.toMutableMap()
        val sources = oldSnapshot.sources.toMutableMap()

        // 更新受影响的属性
        for (attributeId in affectedAttributes) {
            val state = attributeIndex[attributeId]

            // 若当前引擎未持有该属性 说明这次把它移除了
            // 从快照里摘出去
            if (state == null) {
                attributes.remove(attributeId)
                continue
            }

            // 基于新引擎状态算出合并后的结果
            val merged = state.snapshot()

            // 为空说明该属性没有任何来源
            if (merged.isEmpty) {
                attributes.remove(attributeId)
            } else {
                attributes[attributeId] = merged
            }
        }

        // 更新变动的属性源
        for (sourceId in changedSources) {
            val info = sourceIndex[sourceId]
            // null 就是删除来源
            if (info == null) {
                sources.remove(sourceId)
            } else {
                sources[sourceId] = info
            }
        }

        return AttributeEngineSnapshot(
            attributes = attributes,
            sources = sources
        )
    }

}