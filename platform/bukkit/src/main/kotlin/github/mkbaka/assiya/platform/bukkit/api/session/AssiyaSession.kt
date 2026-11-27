package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.platform.bukkit.api.cooldown.Cooldown
import github.mkbaka.assiya.platform.bukkit.api.counter.CounterStore
import github.mkbaka.assiya.platform.bukkit.api.event.AttributeSessionInitEvent
import github.mkbaka.assiya.platform.bukkit.internal.coroutine.BukkitCoroutine
import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import github.mkbaka.assiya.platform.bukkit.internal.feature.BukkitAttributeEngine
import github.mkbaka.assiya.platform.bukkit.internal.util.EventUtils.call
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class AssiyaSession(
    entity: Entity
) {

    @Volatile
    private var ownerReference = WeakReference(entity)

    val owner: Entity?
        get() = ownerReference.get()

    val ownerUUID = entity.uniqueId

    val combatState: CombatState? = if (entity is Player) CombatState(this) else null

    lateinit var attribute: AttributeSession
    val hasAttribute: Boolean get() = this::attribute.isInitialized

    val damageMetaStack = DamageMetaStack()
    val damageProcessorStack = DamageProcessorStack()

    val cooldown = Cooldown()
    val counter = CounterStore()

    val metadata = ConcurrentHashMap<String, Any>()

    internal val dirtyEquipmentSlots = BukkitEquipmentSlot.entries.toMutableSet()
    internal val parsedEquipment = hashMapOf<BukkitEquipmentSlot, AttributeParsedSource>()

    fun initAttribute(engine: BukkitAttributeEngine) {
        val entity = owner ?: return

        attribute = AttributeSession(this, engine)
        attribute.refreshDefaultAttributes()

        if (entity is LivingEntity) {
            EquipmentPipeline.refresh(entity)
        }

        AttributeSessionInitEvent(attribute, entity).call()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getMetadata(key: String): T? {
        return metadata[key] as? T
    }

    fun refresh() {
        if (hasAttribute) attribute.refreshDefaultAttributes()
    }

    fun close() {
        val entity = owner
        ownerReference.clear()
        cooldown.clear()
        counter.clear()
        if (entity != null) {
            EquipmentPipeline.cancel(ownerUUID)
            BukkitCoroutine.launch(entity) {
                combatState?.leave()
                clearEquipment()
            }
        } else {
            clearEquipment()
        }
        if (this::attribute.isInitialized) {
            attribute.close()
        }
    }

    internal fun bindOwner(entity: Entity) {
        require(entity.uniqueId == ownerUUID) { "The entity does not belong to this session" }
        ownerReference = WeakReference(entity)
    }

    internal fun unbindOwner(entity: Entity) {
        if (ownerReference.get() === entity) {
            ownerReference.clear()
        }
    }

    private fun clearEquipment() {
        dirtyEquipmentSlots.clear()
        parsedEquipment.clear()
    }

}
