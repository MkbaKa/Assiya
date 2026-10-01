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
class Health {

    // 原版属性标识
    private static final String MODIFIER_NAME = 'assiya:max-health'
    private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(MODIFIER_NAME.getBytes())

    /**
     * TODO fix 玩家进入服务器后会触发两次 update 调查原因
     */
    static void onUpdate(Attribute attribute, UpdateProcessor processor) {
        TriggerComponent trigger = processor.get(TriggerComponent.@Key)
        if (trigger == null) return
        if (!(trigger.entity instanceof LivingEntity)) return

        processor.get(CallbackComponent.@Key)?.callback {
            Double value = trigger.getValue('max-health', 'total')

            SchedulerUtils.callSync {
                apply((LivingEntity) trigger.entity, value)
            }
        }
    }

    static void apply(LivingEntity owner, Double value) {
        // 过滤掉失效的实体
        if (!owner.isValid()) return
        AttributeInstance instance = owner.getAttribute(VanillaAttribute.GENERIC_MAX_HEALTH)
        if (instance == null) return

        // 删除旧血量
        for (modifier in instance.modifiers) {
            if (modifier.uniqueId == MODIFIER_UUID) {
                instance.removeModifier(modifier)
            }
        }

        // 仅 value 大于 0.0 时增加
        if (value != null && value > 0.0d) {
            instance.addModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, value, AttributeModifier.Operation.ADD_NUMBER))
        }

        // 如果实体当前血量大于增加后的上限
        // 把超出的部分吃掉
        if (owner.health > instance.value) {
            owner.health = instance.value
        }
    }

}
