package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerByMythicSkillComponent
import io.lumine.mythic.api.skills.SkillMetadata

class TriggerBySkillComponent(
    val skillMetadata: SkillMetadata
) : TriggerByMythicSkillComponent
