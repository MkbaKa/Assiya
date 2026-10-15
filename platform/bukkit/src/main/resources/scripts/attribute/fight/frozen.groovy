package scripts.attribute.fight


import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI
import github.mkbaka.assiya.platform.bukkit.api.task.EntityTaskBuilder
import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import groovy.transform.CompileStatic
import org.bukkit.attribute.Attribute as VanillaAttribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.function.BiConsumer

@CompileStatic
class Frozen {

    private static final String MODIFIER_NAME = 'assiya:frozen'
    private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(MODIFIER_NAME.getBytes())

    static void onEnable() {
        BiConsumer<Entity, Map<String, Object>> cleanup = { Entity entity, Map<String, Object> params ->
            removeFrozen(((LivingEntity) entity).getAttribute(VanillaAttribute.GENERIC_MOVEMENT_SPEED))
        }
        EntityTaskAPI.registerTask(MODIFIER_NAME, cleanup, cleanup)
    }

    static void onAttack(Attribute attribute, Processor processor) {
        TriggerComponent attacker = processor.get(TriggerComponent.@Key)
        if (attacker == null) return

        TargetComponent target = processor.get(TargetComponent.@Key)
        if (target == null) return

        double chance = attacker.getValue(attribute.id, 'total', 0.0d)
        if (!ScriptTools.chance(chance)) return

        AttributeInstance instance = target.entity.getAttribute(VanillaAttribute.GENERIC_MOVEMENT_SPEED)
        if (instance == null) return

        double rate = processor.formula('frozen') { Map<String, Object> variables ->
            variables.put('intensity', attacker.getValue('frozen-intensity', 'total', 0.0d))
        }
        double duration = 3.0d + attacker.getValue('frozen-time', 'total', 0.0d)

        removeFrozen(instance)

        if (duration <= 0.0d || rate <= 0.0d) {
            EntityTaskAPI.cancelTask(target.entity, MODIFIER_NAME)
            return
        }

        AttributeModifier modifier = new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, -rate, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
        instance.addModifier(modifier)

        long ticks = (long) Math.ceil(duration * 20.0d)
        EntityTaskAPI.runTask(target.entity, MODIFIER_NAME) { EntityTaskBuilder builder ->
            builder.key(MODIFIER_NAME).delay(ticks)
        }

        ScriptTools.sendCombatMessage(attribute, processor, [frozen: rate * 100.0d, duration: duration])
    }

    private static void removeFrozen(AttributeInstance instance) {
        if (instance == null) return
        for (AttributeModifier modifier : instance.modifiers) {
            if (modifier.uniqueId == MODIFIER_UUID) {
                instance.removeModifier(modifier)
                return
            }
        }
    }

}
