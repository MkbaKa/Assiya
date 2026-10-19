package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import java.util.*

/**
 * processor 执行结束后的回调
 *
 * @param [callback]
 */
class CallbackComponent(
    private val callback: MutableMap<String, Callback> = hashMapOf()
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    @JvmOverloads
    fun callback(
        ignoreCancelled: Boolean = false,
        id: String = UUID.randomUUID().toString(),
        runnable: Runnable
    ) {
        callback[id] = Callback(id, ignoreCancelled, runnable)
    }

    fun invoke(id: String, processor: Processor) {
        val callback = callback[id] ?: return

        if (processor[CancellableComponent.Key]?.isCancelled == true
            && !callback.ignoreCancelled) return

        callback.action.run()
    }

    fun invoke(processor: Processor) {
        for (callback in callback.values) {
            /**
             * 若 processor 持有 [CancellableComponent] 且处于取消
             * 则仅 ignoreCancelled 为 true 的回调可以执行
             */
            if (processor[CancellableComponent.Key]?.isCancelled == true
                && !callback.ignoreCancelled
            ) continue

            try {
                callback.action.run()
            } catch (e: Throwable) {
                logEx(e, "Failed to invoke callback action: ${callback.id}")
            }
        }
    }

    data class Callback(
        val id: String,
        val ignoreCancelled: Boolean,
        val action: Runnable
    )

    companion object Key : ProcessorComponent.Key<CallbackComponent>

}