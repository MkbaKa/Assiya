package github.mkbaka.assiya.platform.bukkit.internal.vanilla

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.LifeCycle
import github.mkbaka.bukkitksp.annotation.Listener
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

internal object VanillaTransientRegistry {
    private data class Entry(
        val attribute: Attribute,
        val instance: AttributeInstance,
        val modifier: AttributeModifier
    )

    private val entries = hashMapOf<UUID, MutableList<Entry>>()

    fun track(entity: LivingEntity, attribute: Attribute, instance: AttributeInstance, modifier: AttributeModifier) {
        entries.getOrPut(entity.uniqueId) { arrayListOf() }.add(Entry(attribute, instance, modifier))
    }

    fun forget(entity: LivingEntity, attribute: Attribute, modifier: AttributeModifier) {
        val owned = entries[entity.uniqueId] ?: return
        owned.removeAll { it.attribute == attribute && it.modifier == modifier }
        if (owned.isEmpty()) entries.remove(entity.uniqueId)
    }

    private fun remove(entity: Entity) {
        val removed = entries.remove(entity.uniqueId) ?: return
        removeAll(removed)
    }

    private fun removeAll(removed: List<Entry>) {
        var failure: RuntimeException? = null
        for (entry in removed) {
            try {
                remove(entry)
            } catch (exception: RuntimeException) {
                val previous = failure
                if (previous == null) failure = exception
                else if (previous !== exception) previous.addSuppressed(exception)
            }
        }
        failure?.let { throw it }
    }

    private fun remove(entry: Entry) {
        // 避免移除其他调用方使用相同身份替换后的修饰器.
        val current = VanillaModifierCompatibility.getModifier(entry.instance, entry.modifier)
        if (current == entry.modifier) {
            entry.instance.removeModifier(entry.modifier)
        }
    }

    @Listener
    fun onQuit(event: PlayerQuitEvent) {
        remove(event.player)
    }

    @Listener(priority = EventPriority.MONITOR)
    fun onRemove(event: EntityRemoveFromWorldEvent) {
        if (event.entity is Player) return
        remove(event.entity)
    }

    // KSP 按优先级升序执行停用回调. 会话与实体任务先关闭.
    @Awake(lifeCycle = LifeCycle.DISABLE, priority = 1000)
    fun close() {
        val removed = entries.values.flatten()
        entries.clear()
        removeAll(removed)
    }
}
