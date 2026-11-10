package scripts.handler

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByKillComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.KillProcessor
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import groovy.transform.CompileStatic
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

@CompileStatic
class Kill {

    static void onEnable() {
        ScriptListener.create(EntityDeathEvent)
                .setEventPriority(EventPriority.HIGH)
                .setIgnoreCancelled(true)
                .setHandler { EntityDeathEvent event ->
                    handle(event)
                }
                .register()
    }

    private static void handle(EntityDeathEvent event) {
        LivingEntity victim = event.entity
        LivingEntity killer = victim.killer
        if (killer == null && victim.lastDamageCause instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) victim.lastDamageCause
            if (damageEvent.damager instanceof LivingEntity) {
                killer = (LivingEntity) damageEvent.damager
            }
        }
        if (killer == null || AssiyaAPI.getSession(killer) == null) return

        KillProcessor processor = KillProcessor.of(killer, victim, event)
        processor.process()

        CancellableComponent cancellable = processor.get(CancellableComponent.@Key)
        TriggerByKillComponent kill = processor.get(TriggerByKillComponent.@Key)

        /**
         * mm v4 可能是基于远古版本的 spigot 开发
         * 所以对 EntityDeathEvent 在 paper 是 Cancellable 这件事毫不知情
         * 于是其死亡监听器没有考虑实体似了又复活是怎么个事儿 并在延迟 60tick 后把它从存活人口里摘了出去
         *
         *  于是这个怪物就变成了没人要的野孩子: "原版怪"
         *
         * 而 v5 他们意识到了这个问题
         * 在监听器的开头加了一行
         * if (ServerVersion.isPaper() && event.isCancelled()) {
         *     return;
         * }
         * 该问题得以修复
         */
        if (cancellable != null && cancellable.isCancelled() && kill.reviveHealth != null) {
            event.cancelled = true
            event.reviveHealth = Math.min(kill.reviveHealth, victim.maxHealth)
        }
    }

}
