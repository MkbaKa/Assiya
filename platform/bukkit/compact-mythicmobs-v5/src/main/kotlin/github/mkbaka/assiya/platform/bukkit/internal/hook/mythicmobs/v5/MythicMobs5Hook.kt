package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5

import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.generated.registerEntries
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.placeholder.MythicPlaceholders
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.util.Utils
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicMobAttributes
import github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.MythicMobsCompatibility
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.on
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import io.lumine.mythic.bukkit.events.MythicReloadedEvent
import io.lumine.mythic.core.skills.SkillCondition
import io.lumine.mythic.core.skills.SkillMechanic
import io.lumine.mythic.core.skills.mechanics.CustomMechanic
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class MythicMobs5Hook : MythicMobsCompatibility, Listener {

    private val mechanics = hashMapOf<String, MethodHandle>()
    private val conditions = hashMapOf<String, MethodHandle>()

    init {
        registerEntries()
    }

    internal fun registerMechanic(type: Class<out SkillMechanic>, vararg names: String) {
        val constructor = MethodHandles.publicLookup().findConstructor(
            type, MethodType.methodType(Void.TYPE, CustomMechanic::class.java, MythicLineConfig::class.java)
        )
        for (name in names) {
            mechanics[name] = constructor
        }
    }

    internal fun registerCondition(type: Class<out SkillCondition>, vararg names: String) {
        val constructor = MethodHandles.publicLookup().findConstructor(
            type, MethodType.methodType(Void.TYPE, MythicLineConfig::class.java)
        )
        for (name in names) {
            conditions[name] = constructor
        }
    }

    override fun register() {
        MythicPlaceholders.register()
        on<MythicConditionLoadEvent>(executor = ::onCondition)
        on<MythicMechanicLoadEvent>(executor = ::onMechanic)
        on<MythicMobSpawnEvent>(priority = EventPriority.MONITOR, executor = ::onSpawn)
        on<MythicReloadedEvent>(priority = EventPriority.MONITOR, executor = ::onReload)
    }

    fun onMechanic(event: MythicMechanicLoadEvent) {
        val constructor = mechanics[event.mechanicName.lowercase()] ?: return
        event.register(constructor.invoke(event.container, event.config) as SkillMechanic)
    }

    fun onCondition(event: MythicConditionLoadEvent) {
        val constructor = conditions[event.conditionName.lowercase()] ?: return
        val condition = constructor.invoke(event.config) as SkillCondition
        Utils.copyAction(event.container, condition)
        event.register(condition)
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
        MythicPlaceholders.register()
        MythicMobAttributes.clear()
    }

}
