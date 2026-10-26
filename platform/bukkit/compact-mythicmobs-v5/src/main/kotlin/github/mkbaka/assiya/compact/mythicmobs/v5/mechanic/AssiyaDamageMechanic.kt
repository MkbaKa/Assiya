package github.mkbaka.assiya.compact.mythicmobs.v5.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v5.handler.component.TriggerBySkillComponent
import github.mkbaka.assiya.compact.mythicmobs.v5.util.SignalUtils
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.Skill
import io.lumine.mythic.api.skills.SkillCaster
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import io.lumine.mythic.core.skills.variables.Variable
import io.lumine.mythic.core.skills.variables.VariableType
import kotlin.jvm.optionals.getOrNull

abstract class AssiyaDamageMechanic(
    container: CustomMechanic,
    mlc: io.lumine.mythic.api.config.MythicLineConfig,
    ignoredKeys: Set<String>
) : SkillMechanic(container.manager, container.file, mlc.line, mlc) {

    protected val preventKnockback: PlaceholderString =
        mlc.getPlaceholderString(arrayOf("preventKnockback", "pkb", "pk"), "false")

    protected val ignoreImmunity: PlaceholderString =
        mlc.getPlaceholderString(arrayOf("ignoreImmunity", "im", "pi", "ii"), "false")

    protected val noDamageTicks: PlaceholderString = mlc.getPlaceholderString(arrayOf("noDamageTicks", "ndt"), "20")

    protected val signal: PlaceholderString = mlc.getPlaceholderString(arrayOf("signal", "s"), "")

    protected var postAttackSkill: Skill? = null

    protected val variables = mlc.entrySet().asSequence().filter { it.key !in ignoredKeys }
        .associate { it.key to PlaceholderString.of(it.value) }

    init {
        val postAttack = mlc.getString(arrayOf("pas", "postAttack", "postAttackSkill"), "")
        if (postAttack.isNotEmpty()) MythicBukkit.inst().skillManager.queueAfterLoad {
            postAttackSkill = MythicBukkit.inst().skillManager.getSkill(postAttack).getOrNull()
        }
        isAsyncSafe = false
    }

    protected fun ProcessorComponentBuilder.applyContext(meta: SkillMetadata, parsed: Map<String, String>) {
        this[CustomDataComponent]?.putAll(parsed)
        this += TriggerBySkillComponent(meta)
    }

    protected fun dispatch(
        meta: SkillMetadata,
        ae: AbstractEntity,
        processor: DamageProcessor,
        realCaster: SkillCaster
    ): io.lumine.mythic.api.skills.SkillResult {
        val damageMeta = DamageMeta(processor).apply {
            ignoreImmunity = this@AssiyaDamageMechanic.ignoreImmunity.get(meta, ae).toBoolean()
            preventKnockback = this@AssiyaDamageMechanic.preventKnockback.get(meta, ae).toBoolean()
            noDamageTicks = this@AssiyaDamageMechanic.noDamageTicks.get(meta, ae).toIntOrNull()
            preDamage = {
                var cancelled = false
                val damage = it.processor.getComponent(DamageMultipleComponent).totalDamage
                val signal = this@AssiyaDamageMechanic.signal.get(meta, ae)
                if (signal.isNotEmpty()) cancelled = SignalUtils.publish(
                    ae, realCaster.entity, signal,
                    hashMapOf("damage-amount" to Variable.ofType(VariableType.FLOAT, damage))
                )
                if (!cancelled && realCaster is ActiveMob) realCaster.lastDamageSkillAmount = damage
                cancelled
            }
            postDamage = {
                postAttackSkill?.let { skill ->
                    val copied = meta.deepClone()
                    copied.trigger = realCaster.entity
                    skill.execute(copied)
                }
            }
        }
        try {
            realCaster.isUsingDamageSkill = true; damageMeta.doDamage()
        } finally {
            realCaster.isUsingDamageSkill = false
        }
        return io.lumine.mythic.api.skills.SkillResult.SUCCESS
    }
}
