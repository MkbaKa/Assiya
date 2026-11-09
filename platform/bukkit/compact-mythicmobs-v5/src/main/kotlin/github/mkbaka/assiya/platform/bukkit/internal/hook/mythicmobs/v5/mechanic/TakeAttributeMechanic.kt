package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.mechanic

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic

@MythicMechanic("assiya:take-attr", "assiya:takeattr")
class TakeAttributeMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : SkillMechanic(container.manager, container.file, mlc.line, mlc), ITargetedEntitySkill {

    private val source = mlc.getPlaceholderString(arrayOf("source", "s"), "")

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val target = ae.bukkitEntity
        val sourceId = source.get(meta, ae)

        AssiyaAPI.getSession(target)?.attribute?.submitRemove(sourceId)
        return SkillResult.SUCCESS
    }

}
