package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByMythicSkillComponent
import io.lumine.xikage.mythicmobs.skills.SkillMetadata

class TriggerBySkillComponent(
    val skillMetadata: SkillMetadata
) : TriggerByMythicSkillComponent
