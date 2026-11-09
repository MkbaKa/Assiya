package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v4.util

import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderMeta
import io.lumine.xikage.mythicmobs.skills.variables.types.AbstractVariable
import java.util.UUID

internal class ProcessorDataSnapshots(
    private val entries: MutableMap<UUID, CustomDataComponent> = hashMapOf()
) : AbstractVariable(entries) {

    companion object {
        private const val VARIABLE = "assiya:processor-data-snapshot"

        fun save(meta: SkillMetadata, uuid: UUID, data: CustomDataComponent) {
            val snapshots = meta.variables.get(VARIABLE) as? ProcessorDataSnapshots
                ?: ProcessorDataSnapshots().also { meta.variables.put(VARIABLE, it) }
            snapshots.entries[uuid] = data
        }

        fun read(meta: PlaceholderMeta, uuid: UUID?): CustomDataComponent? {
            val variables = (meta as? SkillMetadata)?.variables ?: return null
            val snapshots = variables.get(VARIABLE) as? ProcessorDataSnapshots ?: return null
            return snapshots.entries[uuid]
        }
    }

}
