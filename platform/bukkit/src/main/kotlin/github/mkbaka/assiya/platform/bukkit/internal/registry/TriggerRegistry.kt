package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.Trigger
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerSettings
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import java.util.function.BiPredicate
import java.util.function.Consumer

object TriggerRegistry : Registry<TriggerRegistry.TriggerType>("trigger") {

    private val NO_VALIDATION = Consumer<TriggerSettings> {}

    fun register(trigger: Trigger) {
        register(trigger, NO_VALIDATION, null)
    }

    fun register(
        trigger: Trigger,
        hasAttribute: BiPredicate<Processor, String>
    ) {
        register(trigger, NO_VALIDATION, hasAttribute)
    }

    fun register(
        trigger: Trigger,
        validator: Consumer<TriggerSettings>,
        hasAttribute: BiPredicate<Processor, String>?
    ) {
        super.register(
            trigger.id,
            TriggerType(trigger, validator, hasAttribute)
        )
    }

    data class TriggerType(
        val trigger: Trigger,
        val validator: Consumer<TriggerSettings>,
        val hasAttribute: BiPredicate<Processor, String>?
    )

    init {
        register(Trigger.Base("attack", "onAttack", "damage")) { processor, id ->
            processor[TriggerComponent.Key]?.hasAttribute(id) ?: false
        }

        register(Trigger.Base("defense", "onDefense", "damage")) { processor, id ->
            processor[TargetComponent.Key]?.hasAttribute(id) ?: false
        }

        register(Trigger.Base("update", "onUpdate")) { processor, id ->
            processor[TriggerComponent.Key]?.hasAttribute(id) ?: false
        }

        // TODO impl
        register(Trigger.Base("kill", "onKill"))
        register(
            Trigger.Base("timer", "onTimer"),
            { context ->
                val period = context.periodTicks
                require(period != null && period > 0) {
                    "timer trigger period must be a positive number"
                }
            },
            { processor, id ->
                processor[TriggerComponent.Key]?.hasAttribute(id) ?: false
            },
        )
    }

}
