package github.mkbaka.assiya.compact.mythicmobs.v4.mechanic

import github.mkbaka.assiya.compact.mythicmobs.v4.handler.component.TriggerBySkillComponent
import github.mkbaka.assiya.compact.mythicmobs.v4.util.SignalUtils
import github.mkbaka.assiya.platform.bukkit.api.damage.DamageMeta
import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.DamageMultipleComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.processor.DamageProcessor
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.skills.Skill
import io.lumine.xikage.mythicmobs.skills.SkillCaster
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString
import io.lumine.xikage.mythicmobs.skills.variables.Variable
import io.lumine.xikage.mythicmobs.skills.variables.VariableType
import kotlin.jvm.optionals.getOrNull

abstract class AssiyaDamageMechanic(
    mlc: MythicLineConfig,
    ignoredKeys: Set<String>
) : SkillMechanic(mlc.line, mlc) {

    protected val preventKnockback: PlaceholderString =
        mlc.getPlaceholderString(arrayOf("preventKnockback", "pkb", "pk"), "false")

    protected val ignoreImmunity: PlaceholderString =
        mlc.getPlaceholderString(arrayOf("ignoreImmunity", "im", "pi", "ii"), "false")

    protected val noDamageTicks: PlaceholderString = mlc.getPlaceholderString(arrayOf("noDamageTicks", "ndt"), "20")

    protected val signal: PlaceholderString = mlc.getPlaceholderString(arrayOf("signal", "s"), "")

    protected var postAttackSkill: Skill? = null

    protected val variables = mlc.entrySet().asSequence()
        .filter { it.key !in ignoredKeys }
        .associate { it.key to PlaceholderString.of(it.value) }

    init {
        val skillManager = MythicMobs.inst().skillManager
        val postAttack = mlc.getString(arrayOf("pas", "postAttack", "postAttackSkill"), "")

        if (postAttack.isNotEmpty()) {
            skillManager.queueAfterLoad {
                postAttackSkill = skillManager.getSkill(postAttack).getOrNull()
            }
        }

        ASYNC_SAFE = false
    }

    protected fun ProcessorComponentBuilder.applyContext(
        meta: SkillMetadata,
        parsed: Map<String, String>
    ) {
        this[CustomDataComponent.Key]?.putAll(parsed)
        this += TriggerBySkillComponent(meta)
    }

    protected fun dispatch(
        meta: SkillMetadata,
        ae: AbstractEntity,
        processor: DamageProcessor,
        realCaster: SkillCaster
    ): Boolean {
        val damageMeta = DamageMeta(processor).apply {
            ignoreImmunity = this@AssiyaDamageMechanic.ignoreImmunity.get(meta, ae).toBoolean()
            preventKnockback = this@AssiyaDamageMechanic.preventKnockback.get(meta, ae).toBoolean()
            noDamageTicks = this@AssiyaDamageMechanic.noDamageTicks.get(meta, ae).toIntOrNull()

            preDamage = {
                var isCancelled = false
                val damage = it.processor.getComponent(DamageMultipleComponent.Key).totalDamage

                val signal = this@AssiyaDamageMechanic.signal.get(meta, ae)
                if (signal.isNotEmpty()) {
                    isCancelled = SignalUtils.publish(
                        ae, realCaster.entity, signal, hashMapOf(
                            "damage-amount" to Variable.ofType(VariableType.FLOAT, damage)
                        )
                    )
                }

                if (!isCancelled && realCaster is ActiveMob) {
                    realCaster.lastDamageSkillAmount = damage
                }

                isCancelled
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
            realCaster.isUsingDamageSkill = true
            damageMeta.doDamage()
        } finally {
            realCaster.isUsingDamageSkill = false
        }

        return true
    }

}
