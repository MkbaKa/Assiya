package github.mkbaka.assiya.common.engine

import github.mkbaka.assiya.common.engine.expiration.AttributeExpirationScheduler
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshotPublisher
import github.mkbaka.assiya.common.engine.state.AttributeEngineState

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger

/**
 * 基于消息队列的引擎实现
 * 所有消息会被压到一条处理线程执行 不需要锁也能保证线程安全
 * 提供快照供外部并发读取 与消息的写入行为互不冲突
 * 只存在时序问题 异步线程读到的可能是操作前/后的某次结果
 */
open class DefaultAttributeEngine(
    parentScope: CoroutineScope,
    protected val publisher: AttributeEngineSnapshotPublisher,
    protected val logger: Logger
) : AttributeEngine {

    protected val engineJob = SupervisorJob(parentScope.coroutineContext[Job])

    protected val scope = CoroutineScope(parentScope.coroutineContext + engineJob)

    /**
     * 属性值
     */
    protected val state = AttributeEngineState()

    /**
     * 若为调用 refresh 则暂存消息 攒着一起提交
     */
    private val pending = arrayListOf<AttributeEngineAction>()

    /**
     * 在不存在弱智附属代码库库改属性的前提下  MC的使用场景应该做不到撑爆消息队列
     */
    protected val channel = Channel<AttributeEngineAction>(Channel.UNLIMITED)

    /**
     * 临时属性源
     */
    protected val expiration =
        AttributeExpirationScheduler(scope, channel)

    /**
     * 关闭状态
     */
    protected val closed = AtomicBoolean(false)

    /**
     * 启动一个协程任务 不断遍历处理队列内的消息
     * 没消息的时候会自己挂起
     */
    protected val actorJob = scope.launch {
        for (action in channel) {
            handleSafely(action)
        }
    }

    protected fun ensureOpen() {
        check(!closed.get()) { "The engine is closed and cannot process more tasks" }
    }

    protected fun send(action: AttributeEngineAction) {
        check(channel.trySend(action).isSuccess) { "The engine is closed and cannot process more tasks" }
    }

    override fun push(vararg action: AttributeEngineAction) {
        ensureOpen()
        send(AttributeEngineAction.Batch(action.toList()))
    }

    override fun snapshot() = publisher.current()

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            channel.close()
            actorJob.cancel()
            scope.cancel()
        }
    }

    protected fun publish(affectedAttributes: Set<String>, changedSources: Set<String>) {
        if (affectedAttributes.isNotEmpty() || changedSources.isNotEmpty()) {
            publisher.publish(affectedAttributes, changedSources, state.sourceIndex, state.attributeIndex)
        }
    }

    protected open fun removeSource(
        sourceId: String,
        affectedAttributes: MutableSet<String>,
        changedSources: MutableSet<String>
    ) {
        val result = state.remove(sourceId)
        if (result.success) {
            expiration.unbind(sourceId)
            affectedAttributes += result.affectedAttributes
            changedSources += sourceId
        }
    }

    protected fun execute(action: AttributeEngineAction) {
        when (action) {
            is AttributeEngineAction.Batch -> {
                for (child in action.actions) {
                    execute(child)
                }
            }

            is AttributeEngineAction.UpdateSource,
            is AttributeEngineAction.ModifySource,
            is AttributeEngineAction.RemoveSource -> pending += action

            is AttributeEngineAction.Refresh -> {
                val commands = pending.toList()
                pending.clear()
                val context = AttributeUpdateContext(state.copy())
                for (command in commands) {
                    when (command) {
                        is AttributeEngineAction.UpdateSource -> context.submit(command.patch)
                        is AttributeEngineAction.ModifySource -> context.modify(
                            sourceId = command.sourceId,
                            attributes = command.attributes,
                            priority = command.priority,
                            create = command.create,
                            lifeCycle = command.lifeCycle
                        )

                        is AttributeEngineAction.RemoveSource -> context.remove(command.sourceId)
                    }
                }
                refresh(action, context)
            }

            AttributeEngineAction.CheckExpiration -> {
                val affectedAttributes = linkedSetOf<String>()
                val changedSources = linkedSetOf<String>()
                for (sourceId in expiration.expiredSourceIds()) {
                    removeSource(sourceId, affectedAttributes, changedSources)
                }
                publish(affectedAttributes, changedSources)
            }
        }
    }

    protected open fun refresh(
        action: AttributeEngineAction.Refresh,
        context: AttributeUpdateContext
    ) {
        val affectedAttributes = linkedSetOf<String>()
        val changedSources = linkedSetOf<String>()
        for (command in context.commands()) {
            apply(command, affectedAttributes, changedSources)
        }

        publisher.publish(affectedAttributes, changedSources, state.sourceIndex, state.attributeIndex)
    }

    protected fun apply(
        action: AttributeEngineAction,
        affectedAttributes: MutableSet<String>,
        changedSources: MutableSet<String>
    ) {
        when (action) {
            is AttributeEngineAction.UpdateSource -> {
                val diff = state.update(action.patch)
                expiration.bind(action.patch)
                affectedAttributes += diff.affectedAttributes
                changedSources += diff.changedSources
            }

            is AttributeEngineAction.ModifySource -> {
                if (action.sourceId !in state.sourceIndex) return
                val diff = state.modify(action.sourceId, action.attributes, action.priority)
                affectedAttributes += diff.affectedAttributes
                changedSources += diff.changedSources
            }

            is AttributeEngineAction.RemoveSource -> {
                removeSource(action.sourceId, affectedAttributes, changedSources)
            }

            else -> error("Not a source operation: $action")
        }
    }

    protected fun handleSafely(action: AttributeEngineAction) {
        try {
            execute(action)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logger.log(Level.WARNING, "Failed to process engine action $action", e)
        } finally {
            expiration.reschedule()
        }
    }
    
}
