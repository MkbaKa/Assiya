package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.placeholder

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicPlaceholderValues
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.util.ProcessorDataSnapshots
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.utils.serialize.Optl
import io.lumine.mythic.core.mobs.ActiveMob
import io.lumine.mythic.core.skills.placeholders.PlaceholderMeta
import io.lumine.mythic.core.skills.placeholders.types.MetaPlaceholder
import io.lumine.mythic.core.skills.placeholders.types.TargetPlaceholder
import org.bukkit.Bukkit
import java.util.*

object MythicPlaceholders {

    fun register() {
        val manager = MythicBukkit.inst().placeholderManager
        registerValues("attr", MythicPlaceholderValues::attribute)
        registerValues("damagedata", MythicPlaceholderValues::damageData, ::damageData)
        registerValues("metadata", MythicPlaceholderValues::metadata)

        manager.register("owner.uuid", MetaPlaceholder { meta, _ ->
            owner(meta)?.toString() ?: "null"
        })
        manager.register("owner.name", MetaPlaceholder { meta, _ ->
            owner(meta)?.let { Bukkit.getEntity(it)?.name } ?: "null"
        })
    }

    private fun damageData(meta: PlaceholderMeta, uuid: UUID?, args: String): String {
        val data = ProcessorDataSnapshots.read(meta, uuid)
            ?: return MythicPlaceholderValues.damageData(uuid, args)
        val key = args.substringBefore("|")
        return data[key]?.toString() ?: args.substringAfter("|", "null")
    }

    private fun registerValues(
        name: String,
        read: (UUID?, String) -> String,
        readContext: (PlaceholderMeta, UUID?, String) -> String = { _, uuid, args -> read(uuid, args) }
    ) {
        val manager = MythicBukkit.inst().placeholderManager
        manager.register("caster.$name", MetaPlaceholder { meta, args ->
            readContext(meta, meta.caster?.entity?.uniqueId, args)
        })
        manager.register("trigger.$name", MetaPlaceholder { meta, args ->
            readContext(meta, meta.trigger?.uniqueId, args)
        })
        manager.register("target.$name", TargetPlaceholder { meta, target, args ->
            readContext(meta, target?.uniqueId, args)
        })
        manager.register("parent.$name", MetaPlaceholder { meta, args ->
            read(parent(meta), args)
        })
        manager.register("owner.$name", MetaPlaceholder { meta, args ->
            read(owner(meta), args)
        })
    }

    private fun parent(meta: PlaceholderMeta): UUID? {
        val mob = meta.caster as? ActiveMob ?: return null
        val parent = mob.parent
        return if (parent.isPresent) parent.get()?.uniqueId else null
    }

    private fun owner(meta: PlaceholderMeta): UUID? {
        val caster = meta.caster ?: return null
        val active = MythicBukkit.inst().mobManager.getActiveMob(caster.entity.uniqueId)
        if (!active.isPresent) return null
        /**
         * 5.6.2   返回的是 Optl<UUID>
         * 5.13.0  返回 Optl<AbstractEntity>
         */
        val owner: Optl<*> = active.get().owner
        if (!owner.isPresent) return null
        return when (val value: Any = owner.get() ?: return null) {
            is ActiveMob -> value.uniqueId
            is AbstractEntity -> value.uniqueId
            is UUID -> value
            else -> error("Unsupported owner type: ${value.javaClass.name}")
        }
    }

}
