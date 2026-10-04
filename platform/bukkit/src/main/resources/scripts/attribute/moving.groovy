package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import groovy.transform.CompileStatic
import org.bukkit.attribute.Attribute as VanillaAttribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity

@CompileStatic
class Moving {

    private static final String MODIFIER_NAME = 'assiya:moving'
    private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(MODIFIER_NAME.getBytes())

    static void onUpdate(Attribute attribute, UpdateProcessor processor) {
        TriggerComponent trigger = processor.get(TriggerComponent.@Key)
        if (trigger == null) return
        if (!(trigger.entity instanceof LivingEntity)) return

        processor.get(CallbackComponent.@Key)?.callback {
            Double value = trigger.getValue(attribute.id, 'total')
            double amount = 0.0d
            if (value != null) {
                amount = processor.formula('moving') { Map<String, Object> variables ->
                    variables.put('value', value)
                }
            }

            SchedulerUtils.callSync {
                apply((LivingEntity) trigger.entity, amount)
            }
        }
    }

    static void apply(LivingEntity owner, double amount) {
        if (!owner.isValid()) return
        AttributeInstance instance = owner.getAttribute(VanillaAttribute.GENERIC_MOVEMENT_SPEED)
        if (instance == null) return

        for (modifier in instance.modifiers) {
            if (modifier.uniqueId == MODIFIER_UUID) {
                instance.removeModifier(modifier)
            }
        }

        if (amount != 0.0d) {
            instance.addModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, amount, AttributeModifier.Operation.ADD_SCALAR))
        }
    }
}
