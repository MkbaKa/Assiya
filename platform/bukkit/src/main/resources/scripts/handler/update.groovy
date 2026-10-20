package scripts.handler

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeUpdateEvent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import groovy.transform.CompileStatic
import org.bukkit.event.EventPriority

import static github.mkbaka.assiya.platform.bukkit.internal.util.LoggerUtilsKt.logEx

@CompileStatic
class Update {

    static void onEnable() {
        ScriptListener.create(AttributeUpdateEvent.Pre)
                .setEventPriority(EventPriority.HIGHEST)
                .setIgnoreCancelled(true)
                .setHandler { AttributeUpdateEvent.Pre event ->
                    handle(event)
                }
                .register()
    }

    private static void handle(AttributeUpdateEvent.Pre event) {
        try {
            AssiyaSession session = AssiyaAPI.getSession(event.entity)
            if (session == null) return

            UpdateProcessor processor = UpdateProcessor.of(event.entity, session.attribute.snapshot, event.context)
            processor.process()

            CancellableComponent cancellable = processor.get(CancellableComponent.@Key)
            if (cancellable != null && cancellable.isCancelled()) {
                event.cancelled = true
            }
        } catch (Throwable e) {
            logEx(e, "Failed to process attribute update for entity ${event.entity}".toString())
        }
    }

}
