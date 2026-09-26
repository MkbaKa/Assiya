package github.mkbaka.assiya.platform.bukkit.internal.listener

import github.mkbaka.assiya.platform.bukkit.api.event.AttributeUpdateEvent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import github.mkbaka.assiya.platform.bukkit.internal.session.AttributeSessionRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.event.EventPriority

object AttributeUpdateListener {

    @Listener(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onUpdate(event: AttributeUpdateEvent) {
        try {
            val session = AttributeSessionRegistry[event.entity] ?: return
            val processor = UpdateProcessor.of(event.entity, session.snapshot, event.context)

            processor.process()

            if (processor[CancellableComponent.Key]?.isCancelled == true) {
                event.isCancelled = true
            }
        } catch (e: Throwable) {
            logEx(e, "Failed to process attribute update for entity ${event.entity}")
        }
    }

    // TODO timer trigger impl
//    @Awake(LifeCycle.ENABLE, priority = 114514)
//    fun schedule() {
//        SchedulerUtils.callAsyncTimer(0, 1) {
//
//        }
//    }

}