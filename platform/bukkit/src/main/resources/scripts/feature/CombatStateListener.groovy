package scripts.feature

import github.mkbaka.assiya.platform.bukkit.api.CombatStateAPI
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import github.mkbaka.assiya.platform.bukkit.internal.util.LoggerUtilsKt
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
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

                    // 战斗状态只对玩家生效
                    if (damager instanceof Player) {
                        try {
                            CombatStateAPI.join((Player) damager, entity)
                        } catch (Throwable ex) {
                            LoggerUtilsKt.logEx(ex, "Failed to enter combat for damager ${damager.uniqueId} against target ${entity.uniqueId}")
                        }
                    }

                    if (entity instanceof Player) {
                        try {
                            CombatStateAPI.join((Player) entity, damager)
                        } catch (Throwable ex) {
                            LoggerUtilsKt.logEx(ex, "Failed to enter combat for damaged entity ${entity.uniqueId} against target ${damager.uniqueId}")
                        }
                    }
                }
                .register()
    }

}
