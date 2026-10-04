package scripts.attribute.fight


import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptTools
import github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils
import groovy.transform.CompileStatic
import org.bukkit.attribute.Attribute as VanillaAttribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.scheduler.BukkitTask

@CompileStatic
class Frozen {

    private static final String MODIFIER_NAME = 'assiya:frozen'
    private static final UUID MODIFIER_UUID = UUID.nameUUIDFromBytes(MODIFIER_NAME.getBytes())
    private static final Map<UUID, Effect> EFFECTS = new HashMap<>()

    static void onDisable() {
        for (effect in EFFECTS.values()) {
            effect.task.cancel()
            effect.instance.removeModifier(effect.modifier)
        }
        EFFECTS.clear()
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
        UUID uuid = target.entity.uniqueId
        Effect previous = EFFECTS.remove(uuid)
        if (previous != null) {
            previous.task.cancel()
            previous.instance.removeModifier(previous.modifier)
        }
        if (duration <= 0.0d || rate <= 0.0d) return

        AttributeModifier modifier = new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, -rate, AttributeModifier.Operation.MULTIPLY_SCALAR_1)
        instance.addModifier(modifier)
        long ticks = (long) Math.ceil(duration * 20.0d)
        // TODO impl entity task with coroutine
        BukkitTask task = SchedulerUtils.callSyncLater(ticks) {
            EFFECTS.remove(uuid)
            instance.removeModifier(modifier)
        }
        EFFECTS.put(uuid, new Effect(instance, modifier, task))
        ScriptTools.sendCombatMessage(attribute, processor, [frozen: rate * 100.0d, duration: duration])
    }

    private static class Effect {
        final AttributeInstance instance
        final AttributeModifier modifier
        final BukkitTask task

        Effect(AttributeInstance instance, AttributeModifier modifier, BukkitTask task) {
            this.instance = instance
            this.modifier = modifier
            this.task = task
        }
    }
}
