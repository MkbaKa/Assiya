package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.mechanic

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.util.ProcessorDataSnapshots
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic

@MythicMechanic("assiya:processor-data-snapshot")
class ProcessorDataSnapshotMechanic(
    container: CustomMechanic,
    mlc: MythicLineConfig
) : SkillMechanic(container.manager, container.file, mlc.line, mlc), ITargetedEntitySkill {

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): SkillResult {
        val processor = AssiyaAPI.getSession(ae.uniqueId)?.damageProcessorStack?.current
            ?: return SkillResult.CONDITION_FAILED
        val data = processor[CustomDataComponent] ?: return SkillResult.CONDITION_FAILED
        ProcessorDataSnapshots.save(meta, ae.uniqueId, data)
        return SkillResult.SUCCESS
    }

}
