package github.mkbaka.assiya.compact.mythicmobs.v4

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicMobAttributes
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicMobsCompatibility
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.on
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent
import org.bukkit.event.EventPriority

class MythicMobs4Hook : MythicMobsCompatibility {

    override fun register() {
        on<MythicMobSpawnEvent>(priority = EventPriority.MONITOR, executor = ::onSpawn)
        on<MythicReloadedEvent>(priority = EventPriority.MONITOR, executor = ::onReload)
    }

    fun onSpawn(event: MythicMobSpawnEvent) {
        if (event.isCancelled) return
        val mob = event.mobType
        val config = mob.config
        MythicMobAttributes.apply(
            entity = event.entity,
            mobId = mob.internalName,
            level = event.mobLevel,
            attributes = config.getStringList("attributes"),
            calculator = config.getBoolean("calculator-attribute", false)
        )
    }

    fun onReload(event: MythicReloadedEvent) {
        MythicMobAttributes.clear()
    }

}
