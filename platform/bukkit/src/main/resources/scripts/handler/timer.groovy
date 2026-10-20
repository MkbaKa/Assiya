package scripts.handler

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.TimerProcessor
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry.Subscriber
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptScheduler
import groovy.transform.CompileStatic
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

import java.util.concurrent.atomic.AtomicLong

import static github.mkbaka.assiya.platform.bukkit.internal.util.LoggerUtilsKt.logEx

@CompileStatic
class Timer {

    private static final AtomicLong TICKS = new AtomicLong()

    static void onEnable() {
        ScriptScheduler.create()
                .setAsync(true)
                .setDelay(0L)
                .setPeriod(1L)
                .setHandler { tick() }
                .register()
    }

    private static void tick() {
        long resetPeriod = Config.global.TIMER_RESET_PERIOD
        long tick = TICKS.incrementAndGet()
        List<Subscriber> subscribers = HandlerRegistry.select('timer') { Subscriber subscriber ->
            Long period = subscriber.settings.periodTicks
            if (period == null) throw new IllegalArgumentException('timer trigger requires period')
            return tick % period == 0L
        }

        if (subscribers.isEmpty()) {
            if (tick >= resetPeriod) TICKS.compareAndSet(tick, 0L)
            return
        }

        for (Player player in Bukkit.getOnlinePlayers()) {
            AssiyaSession session = AssiyaAPI.getSession(player.uniqueId)
            if (session == null) continue
            if (!session.hasAttribute || session.attribute.isClosed()) continue
            Entity owner = session.owner
            if (owner == null) continue

            try {
                TimerProcessor.of(owner, session.attribute.snapshot, subscribers).process()
            } catch (Throwable e) {
                logEx(e, "Failed to process timer for entity ${session.ownerUUID}".toString())
            }
        }

        if (tick >= resetPeriod) TICKS.compareAndSet(tick, 0L)
    }

}
