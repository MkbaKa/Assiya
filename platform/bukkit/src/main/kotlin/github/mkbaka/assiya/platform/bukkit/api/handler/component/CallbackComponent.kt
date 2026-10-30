package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.handler.ForkableComponent
import github.mkbaka.assiya.common.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import java.util.*

/**
 * processor 执行结束后的回调
 *
 * @param [callbacks]
 */
class CallbackComponent(
    callbacks: Map<String, Callback> = emptyMap()
) : ProcessorComponent, ForkableComponent<CallbackComponent> {

    private val container = Container(HashMap(callbacks), ArrayList(callbacks.values))

    override val key: ProcessorComponent.Key<*> = Key

    @JvmOverloads
    fun callback(
        ignoreCancelled: Boolean = false,
        id: String = UUID.randomUUID().toString(),
        runnable: Runnable
    ) {
        container.register(id, Callback(id, ignoreCancelled, runnable))
    }

    fun invoke(id: String, processor: BukkitProcessor) {
        val callback = container.callbacks[id] ?: return

        if (processor[CancellableComponent]?.isCancelled == true
            && !callback.ignoreCancelled
        ) return

        callback.action.run()
    }

    fun invoke(processor: BukkitProcessor) {
        for (callback in container.loop) {
            /**
             * 若 processor 持有 [CancellableComponent] 且处于取消
             * 则仅 ignoreCancelled 为 true 的回调可以执行
             */
            if (processor[CancellableComponent]?.isCancelled == true
                && !callback.ignoreCancelled
            ) continue

            try {
                callback.action.run()
            } catch (e: Throwable) {
                logEx(e, "Failed to invoke callback action: ${callback.id}")
            }
        }
    }

    override fun fork(): CallbackComponent = CallbackComponent(container.callbacks)

    private class Container(
        val callbacks: MutableMap<String, Callback>,
        val loop: ArrayList<Callback>
    ) {

        fun register(id: String, callback: Callback) {
            callbacks[id] = callback
            loop.add(callback)
        }

    }

    data class Callback(
        val id: String,
        val ignoreCancelled: Boolean,
        val action: Runnable
    )

    companion object Key : ProcessorComponent.Key<CallbackComponent>

}