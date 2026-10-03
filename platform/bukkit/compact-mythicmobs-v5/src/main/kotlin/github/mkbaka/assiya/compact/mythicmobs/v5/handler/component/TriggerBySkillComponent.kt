package github.mkbaka.assiya.compact.mythicmobs.v5.handler.component

import github.mkbaka.assiya.platform.bukkit.api.handler.ProcessorComponent
import io.lumine.mythic.api.skills.SkillMetadata

class TriggerBySkillComponent(
    val skillMetadata: SkillMetadata
) : ProcessorComponent {

    override val key: ProcessorComponent.Key<*> = Key

    companion object Key : ProcessorComponent.Key<TriggerBySkillComponent>

}