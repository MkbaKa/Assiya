package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.task.EntityTaskBuilder
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributeView
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils
import groovy.transform.CompileStatic
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

import java.util.function.BiConsumer

@CompileStatic
class Frozen {

    private static final String MODIFIER_NAME = 'assiya:frozen'

    static void onEnable() {
        BiConsumer<Entity, Map<String, Object>> cleanup = { Entity entity, Map<String, Object> params ->
            VanillaAttributes.of((LivingEntity) entity).movementSpeed.removeModifier(MODIFIER_NAME)
        }
        EntityTaskAPI.registerTask(MODIFIER_NAME, cleanup, cleanup)
    }

    static void onAttack(Attribute attribute, BukkitProcessor processor) {
        TriggerComponent attacker = processor.get(TriggerComponent.@Key)
        if (attacker == null) return

        TargetComponent target = processor.get(TargetComponent.@Key)
        if (target == null) return

        double chance = attacker.getValue(attribute.id, 'total', 0.0d)
        if (!ScriptTools.chance(chance)) return

        VanillaAttributeView instance = VanillaAttributes.of(target.entity).movementSpeed

        double rate = processor.formula('frozen') { Map<String, Object> variables ->
            variables.put('intensity', attacker.getValue('frozen-intensity', 'total', 0.0d))
        }
        double duration = 3.0d + attacker.getValue('frozen-time', 'total', 0.0d)

        if (duration <= 0.0d || rate <= 0.0d) {
            instance.removeModifier(MODIFIER_NAME)
            EntityTaskAPI.cancelTask(target.entity, MODIFIER_NAME)
            return
        }

        instance.setTransient(MODIFIER_NAME, -rate, AttributeModifier.Operation.MULTIPLY_SCALAR_1)

        long ticks = (long) Math.ceil(duration * 20.0d)
        EntityTaskAPI.runTask(target.entity, MODIFIER_NAME) { EntityTaskBuilder builder ->
            builder.key(MODIFIER_NAME).delay(ticks)
        }

        CustomDataComponent customData = processor.get(CustomDataComponent.@Key)
        if (customData != null) {
            customData.put("is-frozen", true)
            customData.put("frozen-rate", rate)
            customData.put("frozen-duration", duration)
        }

        ScriptTools.sendCombatMessage(
                attribute,
                processor,
                [frozen: StringUtils.format(rate * 100.0d, 2), duration: StringUtils.format(duration, 0)]
        )
    }

}
