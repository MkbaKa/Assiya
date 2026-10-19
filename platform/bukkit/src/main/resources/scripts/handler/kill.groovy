package scripts.handler

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.KillProcessor
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import groovy.transform.CompileStatic
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent

@CompileStatic
class Kill {

    static void onEnable() {
        ScriptListener.create(EntityDamageByEntityEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { EntityDamageByEntityEvent event ->
                    handle(event)
                }
                .register()
    }

    private static void handle(EntityDamageByEntityEvent event) {
        if (!(event.entity instanceof LivingEntity)) return

        LivingEntity victim = (LivingEntity) event.entity
        if (event.finalDamage <= victim.health) return

        if (AssiyaAPI.getSession(event.damager) == null) return

        KillProcessor.of(event.damager, victim, event).process()
    }

}
