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
class Health {

    // 原版属性标识
    private static final String MODIFIER_NAME = 'assiya:max-health'

    static void onUpdate(Attribute attribute, UpdateProcessor processor) {
        Entity entity = processor.session.owner
        if (!(entity instanceof LivingEntity)) return

        processor.get(CallbackComponent.@Key)?.callback {
            Double value = processor.getValue('max-health', 'total')

            processor.<LivingEntity> launch((LivingEntity) entity) { LivingEntity target ->
                apply(target, value)
            }
        }
    }

    static void apply(LivingEntity owner, Double value) {
        // 过滤掉失效的实体
        if (!owner.isValid()) return
        VanillaAttributeView instance = VanillaAttributes.of(owner).maxHealth

        // 仅 value 大于 0.0 时增加.
        if (value != null && value > 0.0d) {
            instance.setTransient(MODIFIER_NAME, value, AttributeModifier.Operation.ADD_NUMBER)
        } else {
            instance.removeModifier(MODIFIER_NAME)
        }

        // 如果实体当前血量大于增加后的上限
        // 把超出的部分吃掉
        if (owner.health > instance.value) {
            owner.health = instance.value
        }
    }

}
