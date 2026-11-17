package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.mechanic

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata

@MythicMechanic("assiya:take-attr", "assiya:takeattr")
class TakeAttributeMechanic(
    mlc: MythicLineConfig
) : SkillMechanic(mlc.line, mlc), ITargetedEntitySkill {

    private val source = mlc.getPlaceholderString(arrayOf("source", "s"), "")

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        val target = ae.bukkitEntity
        val sourceId = source.get(meta, ae)

        AssiyaAPI.takeAttribute(target, sourceId)
        return true
    }

}
