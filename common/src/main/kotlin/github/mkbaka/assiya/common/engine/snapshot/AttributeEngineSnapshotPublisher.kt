package github.mkbaka.assiya.common.engine.snapshot

import github.mkbaka.assiya.common.engine.state.AttributeState
import github.mkbaka.assiya.common.source.AttributeSourceInfo

interface AttributeEngineSnapshotPublisher {

    fun current(): AttributeEngineSnapshot

    fun publish(
        affectedAttributes: Set<String>,
        changedSources: Set<String>,
        sourceIndex: Map<String, AttributeSourceInfo>,
        attributeIndex: Map<String, AttributeState<*>>,
    )

}