package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributeView
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import groovy.transform.CompileStatic
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

@CompileStatic
class Moving {

    private static final String MODIFIER_NAME = 'assiya:moving'

    static void onUpdate(Attribute attribute, UpdateProcessor processor) {
        Entity entity = processor.session.owner
        if (!(entity instanceof LivingEntity)) return

        processor.get(CallbackComponent.@Key)?.callback {
            Double value = processor.getValue(attribute.id, 'total')
            double amount = 0.0d
            if (value != null) {
                amount = processor.formula('moving') { Map<String, Object> variables ->
                    variables.put('value', value)
                }
            }

            double resolvedAmount = amount
            processor.<LivingEntity> launch((LivingEntity) entity) { LivingEntity target ->
                apply(target, resolvedAmount)
            }
        }
    }

    static void apply(LivingEntity owner, double amount) {
        if (!owner.isValid()) return
        VanillaAttributeView instance = VanillaAttributes.of(owner).movementSpeed

        if (amount != 0.0d) {
            instance.setTransient(MODIFIER_NAME, amount, AttributeModifier.Operation.ADD_SCALAR)
        } else {
            instance.removeModifier(MODIFIER_NAME)
        }
    }
}
