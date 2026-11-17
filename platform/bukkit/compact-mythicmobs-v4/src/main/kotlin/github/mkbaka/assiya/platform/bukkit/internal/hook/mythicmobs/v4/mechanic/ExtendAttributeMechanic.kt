package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.mechanic

import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import kotlin.time.Duration.Companion.milliseconds

@MythicMechanic("assiya:extend-attr", "assiya:extendattr")
class ExtendAttributeMechanic(
    mlc: MythicLineConfig
) : SkillMechanic(mlc.line, mlc), ITargetedEntitySkill {

    private val reverse = mlc.getPlaceholderString(arrayOf("reverse"), "false")
    private val duration = mlc.getPlaceholderString(arrayOf("duration", "d"), "-1")

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
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

        val session = AssiyaAPI.getSessionOrCreate(receiver)
        AssiyaAPI.addAttributeSnapshot(session, snapshot, lifeCycle)
        return true
    }

}
