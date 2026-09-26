package github.mkbaka.assiya.platform.bukkit.internal.config.obj

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.WrappedAttribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.HandlerComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerHandler
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerSettings
import github.mkbaka.assiya.platform.bukkit.internal.config.ScriptManager

class ConfigAttribute(
    override val id: String,
    reader: ReaderComponent?,
    triggers: Map<Trigger, ConfigTrigger>,
) : Attribute {

    override val components: Map<AttributeComponent.Key<*>, AttributeComponent>

    init {
        val components = arrayListOf<AttributeComponent>()
        if (reader != null) {
            components += reader
        }
        if (triggers.isNotEmpty()) {
            components += HandlerComponent(
                triggers.mapValues { (trigger, config) ->
                    TriggerHandler(config.settings) { processor ->


                        val handler = config.settings.handler!!
                        ScriptManager.invoke(handler, trigger.function, WrappedAttribute(this), processor)
                    }
                }
            )
        }
        this.components = components.associateBy { it.key }
    }

    class ConfigTrigger(
        val settings: TriggerSettings,
    )

}
