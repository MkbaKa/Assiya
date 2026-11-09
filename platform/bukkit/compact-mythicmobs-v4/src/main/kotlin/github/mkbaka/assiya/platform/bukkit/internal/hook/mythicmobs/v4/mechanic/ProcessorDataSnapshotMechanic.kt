package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.mechanic

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.util.ProcessorDataSnapshots
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.mythicksp.annotation.MythicMechanic
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata

@MythicMechanic("assiya:processor-data-snapshot")
class ProcessorDataSnapshotMechanic(
    mlc: MythicLineConfig
) : SkillMechanic(mlc.line, mlc), ITargetedEntitySkill {

    override fun castAtEntity(meta: SkillMetadata, ae: AbstractEntity): Boolean {
        val processor = AssiyaAPI.getSession(ae.uniqueId)?.damageProcessorStack?.current ?: return false
        val data = processor[CustomDataComponent] ?: return false
        ProcessorDataSnapshots.save(meta, ae.uniqueId, data)
        return true
    }

}
