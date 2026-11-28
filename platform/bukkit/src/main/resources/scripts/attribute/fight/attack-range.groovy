package scripts.attribute.fight

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.UpdateProcessor
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributeView
import github.mkbaka.assiya.platform.bukkit.api.vanilla.VanillaAttributes
import github.mkbaka.assiya.platform.bukkit.internal.vanilla.VanillaAttribute
import groovy.transform.CompileStatic
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * 对原版实体触及距离属性的映射
 * 只支持 1.20.5+
 */
@CompileStatic
class AttackRange {

    private static final String MODIFIER_NAME = 'assiya:attack-range'

    private static final String VANILLA_ATTRIBUTE_NAME = 'entity_interaction_range'

    private static boolean enable;

    static void onEnable() {
        enable = VanillaAttribute.get(VANILLA_ATTRIBUTE_NAME) != null
    }

    static void onUpdate(Attribute attribute, UpdateProcessor processor) {
        if (!enable) return

        Entity owner = processor.session.owner
        if (!(owner instanceof Player)) return

        processor.get(CallbackComponent.@Key)?.callback {
            Double value = processor.getValue(attribute.id, 'total')
            double total = value != null ? value : 0.0d
            processor.<Player> launch((Player) owner) { Player player ->
                if (!player.isValid()) return
                VanillaAttributeView view = VanillaAttributes.of(player).find(VANILLA_ATTRIBUTE_NAME)
                if (view == null) return
                if (total > 0.0d) {
                    view.setTransient(MODIFIER_NAME, total, AttributeModifier.Operation.ADD_NUMBER)
                } else {
                    view.removeModifier(MODIFIER_NAME)
                }
            }
        }
    }

}
