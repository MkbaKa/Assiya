package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerSettings
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import java.util.function.BiPredicate
import java.util.function.Consumer

object TriggerRegistry : NamedRegistry<TriggerRegistry.TriggerType>("trigger") {

    private val NO_VALIDATION = Consumer<TriggerSettings> {}

    fun register(trigger: Trigger) {
        register(trigger, NO_VALIDATION, null)
    }

    fun register(
        trigger: Trigger,
        hasAttribute: BiPredicate<BukkitProcessor, String>
    ) {
        register(trigger, NO_VALIDATION, hasAttribute)
    }

    fun register(
        trigger: Trigger,
        validator: Consumer<TriggerSettings>,
        hasAttribute: BiPredicate<BukkitProcessor, String>?
    ) {
        super.register(
            trigger.id,
            TriggerType(trigger, validator, hasAttribute)
        )
    }

    data class TriggerType(
        val trigger: Trigger,
        val validator: Consumer<TriggerSettings>,
        val hasAttribute: BiPredicate<BukkitProcessor, String>?
    )

    init {
        register(Trigger.Base("attack", "onAttack", "damage")) { processor, id ->
            processor[TriggerComponent]?.hasAttribute(id) ?: false
        }

        register(Trigger.Base("defense", "onDefense", "damage")) { processor, id ->
            processor[TargetComponent]?.hasAttribute(id) ?: false
        }

        register(Trigger.Base("kill", "onKill")) { processor, id ->
            processor[TriggerComponent]?.hasAttribute(id) ?: false
        }

        register(Trigger.Base("update", "onUpdate")) { processor, id ->
            processor[TriggerComponent]?.hasAttribute(id) ?: false
        }

        register(
            Trigger.Base("timer", "onTimer"),
            { context ->
                val period = context.periodTicks
                require(period != null && period > 0) {
                    "timer trigger period must be a positive number"
                }
            },
            { processor, id ->
                processor[TriggerComponent]?.hasAttribute(id) ?: false
            },
        )
    }

}
