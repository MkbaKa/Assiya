package scripts.feature

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import github.mkbaka.assiya.platform.bukkit.internal.util.LoggerUtilsKt
import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.projectiles.ProjectileSource

class CombatStateListener {

    static void onEnable() {
        ScriptListener.create(EntityDamageByEntityEvent)
                .setEventPriority(EventPriority.LOW)
                .setIgnoreCancelled(true)
                .setHandler { EntityDamageByEntityEvent event ->
                    Entity damager = event.damager
                    Entity entity = event.entity

                    if (damager instanceof Projectile) {
                        ProjectileSource shooter = ((Projectile) damager).shooter
                        if (shooter instanceof Entity) {
                            damager = (Entity) shooter
                        }
                    }

                    try {
                        AssiyaAPI.getSession(damager)?.combatState?.join(entity)
                    } catch (Throwable ex) {
                        LoggerUtilsKt.logEx(ex, "Failed to enter combat for damager ${damager.uniqueId} against target ${entity.uniqueId}")
                    }

                    try {
                        AssiyaAPI.getSession(entity)?.combatState?.join(damager)
                    } catch (Throwable ex) {
                        LoggerUtilsKt.logEx(ex, "Failed to enter combat for damaged entity ${entity.uniqueId} against target ${damager.uniqueId}")
                    }
                }
                .register()
    }

}
