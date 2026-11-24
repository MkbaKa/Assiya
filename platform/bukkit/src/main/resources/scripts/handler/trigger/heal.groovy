package scripts.handler.trigger

import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.TriggerRegistry
import groovy.transform.CompileStatic

/**
 * 触发器注册示例
 */
@CompileStatic
class Heal {

    private TriggerRegistry.TriggerType registration

    void onEnable() {
        TriggerRegistry.INSTANCE.register(new Trigger.Base('heal', 'onHeal', 'heal')) {
            BukkitProcessor processor, String id ->
                return processor.get(TriggerComponent.@Key)?.hasAttribute(id) ?: false
        }
        registration = TriggerRegistry.INSTANCE.get('heal')
    }

    void onDisable() {
        if (registration == null) return
        TriggerRegistry.INSTANCE.unregister('heal', registration)
        registration = null
    }

}
