package github.mkbaka.assiya.compact.mythicmobs.v5.mechanic

import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import kotlin.time.Duration.Companion.milliseconds

@MythicMechanic("assiya:extend-attr", "assiya:extendattr")
class ExtendAttributeMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : SkillMechanic(container.manager, container.file, mlc.line, mlc), ITargetedEntitySkill {

    private val reverse = mlc.getPlaceholderString(arrayOf("reverse"), "false")
    private val duration = mlc.getPlaceholderString(arrayOf("duration", "d"), "-1")

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val caster = meta.caster.entity.bukkitEntity
        val target = ae.bukkitEntity
        val reverse = reverse.get(meta, ae).toBoolean()
        val ticks = duration.get(meta, ae).toLong()
        val lifeCycle = if (ticks == -1L) {
            AttributeSourceLifecycle.Stable
        } else {
            AttributeSourceLifecycle.Temporary(50.milliseconds * ticks.toDouble())
        }

        val source = if (reverse) caster else target
        val receiver = if (reverse) target else caster
        val snapshot = AssiyaAPI.getSnapshot(source)
        AssiyaAPI.getSessionOrCreate(receiver).submitSnapshot(snapshot, lifeCycle)
        return SkillResult.SUCCESS
    }

}
