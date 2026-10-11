package github.mkbaka.assiya.platform.bukkit.internal.listener

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeUpdateEvent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.TimerProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.Bukkit
import org.bukkit.event.EventPriority
import java.util.concurrent.atomic.AtomicLong

object AttributeUpdateListener {

    @Listener(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onUpdate(event: AttributeUpdateEvent.Pre) {
        try {
            val session = AssiyaAPI.getSession(event.entity) ?: return
            val processor = UpdateProcessor.of(event.entity, session.attribute.snapshot, event.context)

            processor.process()

            if (processor[CancellableComponent.Key]?.isCancelled == true) {
                event.isCancelled = true
            }
        } catch (e: Throwable) {
            logEx(e, "Failed to process attribute update for entity ${event.entity}")
        }
    }

    private val ticks = AtomicLong()

    @Awake(lifeCycle = LifeCycle.ENABLE)
    fun register() {
        SchedulerUtils.callAsyncTimer(0, 1) {
            val resetPeriod = Config.global.TIMER_RESET_PERIOD
            val tick = ticks.incrementAndGet()
            val subscribers = HandlerRegistry.select("timer") { subscriber ->
                val period = requireNotNull(subscriber.settings.periodTicks) {
                    "timer trigger requires period"
                }
                tick % period == 0L
            }

            if (subscribers.isEmpty()) {
                if (tick >= resetPeriod) ticks.compareAndSet(tick, 0L)
                return@callAsyncTimer
            }

            for (player in Bukkit.getOnlinePlayers()) {
                val session = AssiyaAPI.getSession(player.uniqueId) ?: continue
                if (!session.hasAttribute || session.attribute.isClosed) continue
                val owner = session.owner ?: continue

                try {
                    TimerProcessor.of(owner, session.attribute.snapshot, subscribers).process()
                } catch (e: Throwable) {
                    logEx(e, "Failed to process timer for entity ${session.ownerUUID}")
                }

            }

            if (tick >= resetPeriod) {
                ticks.compareAndSet(tick, 0L)
            }
        }
    }

}
