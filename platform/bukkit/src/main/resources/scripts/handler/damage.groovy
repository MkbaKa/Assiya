package scripts.handler

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByEventComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import github.mkbaka.assiya.platform.bukkit.internal.damage.DamageMetaStack
import github.mkbaka.assiya.platform.bukkit.internal.metadata.MetadataRegistry
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import groovy.transform.CompileStatic
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.projectiles.ProjectileSource

@CompileStatic
class Damage {

    private static final String FORCE_METADATA_KEY = "Assiya:force"

    static void onEnable() {
        ScriptListener.create(ProjectileLaunchEvent)
                .setEventPriority(EventPriority.HIGHEST)
                .setIgnoreCancelled(true)
                .setHandler { ProjectileLaunchEvent event ->
                    Projectile projectile = event.entity
                    ProjectileSource shooter = projectile.shooter
                    if (!(shooter instanceof LivingEntity)) return

                    AttributeEngineSnapshot shooterSnapshot = snapshot((LivingEntity) shooter)
                    AssiyaAPI.getSessionOrCreate(projectile).submitSnapshot(shooterSnapshot)
                }
                .register()

        ScriptListener.create(EntityShootBowEvent)
                .setEventPriority(EventPriority.HIGHEST)
                .setIgnoreCancelled(true)
                .setHandler { EntityShootBowEvent event ->
                    MetadataRegistry.setMetadata(event.projectile.uniqueId, FORCE_METADATA_KEY, event.getForce())
                }
                .register()

        ScriptListener.create(EntityDamageByEntityEvent)
                .setEventPriority(EventPriority.HIGHEST)
                .setIgnoreCancelled(true)
                .setHandler { EntityDamageByEntityEvent event ->
                    handle(event)
                }
                .register()
    }

    private static void handle(EntityDamageByEntityEvent event) {
        // 不处理自定义伤害
        if (event.cause == EntityDamageEvent.DamageCause.CUSTOM) return

        // 不处理展示框之类的非生物实体
        if (!(event.entity instanceof LivingEntity)) return

        // 避免递归
        if (DamageMetaStack.getCurrent(event.damager.uniqueId) != null) return

        LivingEntity defender = (LivingEntity) event.entity
        DamageProcessor processor

        /**
         * 远程伤害以实际造成伤害的抛射物为 attacker
         * 如果要获取发射者 应使用 [github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent]#shooter \
         * 取对应 TriggerComponent 实例
         */
        if (event.cause == EntityDamageEvent.DamageCause.PROJECTILE && event.damager instanceof Projectile) {
            Projectile projectile = (Projectile) event.damager
            ProjectileSource shooter = projectile.shooter
            if (!(shooter instanceof LivingEntity)) return

            // 取发射时的蓄力力度
            // 若不存在则默认 1.0
            Float force = MetadataRegistry.getMetadata(projectile.uniqueId, FORCE_METADATA_KEY)
            if (force == null) force = 1.0f

            TriggerComponent shooterComponent = new TriggerComponent((LivingEntity) shooter, snapshot((LivingEntity) shooter))

            processor = DamageProcessor.ofProjectile(
                    projectile, defender, snapshot(projectile), snapshot(defender), shooterComponent, force
            ) { ProcessorComponentBuilder builder ->
                builder.add(new TriggerByEventComponent(event))
                builder.get(DamageMultipleComponent.@Key).setDamage('vanilla', event.finalDamage)
            }
        } else {
            // 正常攻击伤害
            if (!(event.damager instanceof LivingEntity)) return

            LivingEntity attacker = (LivingEntity) event.damager
            processor = DamageProcessor.ofAttack(
                    attacker, defender, snapshot(attacker), snapshot(defender)
            ) { ProcessorComponentBuilder builder ->
                builder.add(new TriggerByEventComponent(event))
                builder.get(DamageMultipleComponent.@Key).setDamage('vanilla', event.finalDamage)
            }
        }

        processor.process()

        // 事件同步 processor 的取消状态
        CancellableComponent cancellable = processor.get(CancellableComponent.@Key)
        if (cancellable != null && cancellable.isCancelled()) {
            event.cancelled = true
            return
        }

        // 应用伤害
        event.damage = processor.getComponent(DamageMultipleComponent.@Key).totalDamage
    }

    private static AttributeEngineSnapshot snapshot(Entity entity) {
        AttributeEngineSnapshot snapshot = AssiyaAPI.getSession(entity.uniqueId)?.snapshot
        return snapshot != null ? snapshot : AttributeEngineSnapshot.EMPTY
    }

}
