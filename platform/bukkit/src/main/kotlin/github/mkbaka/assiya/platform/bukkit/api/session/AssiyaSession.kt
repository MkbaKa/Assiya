package github.mkbaka.assiya.platform.bukkit.api.session

import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.feature.BukkitAttributeEngine
import github.mkbaka.assiya.platform.bukkit.internal.util.entityOf
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class AssiyaSession(
    entity: Entity
) {

    val owner: Entity?
        get() = entityOf(ownerUUID)

    val ownerUUID = entity.uniqueId

    val combatState: CombatState? = if (entity is Player) CombatState(this) else null

    lateinit var attribute: AttributeSession
    val hasAttribute: Boolean
        get() = this::attribute.isInitialized

    val damageMetaStack = DamageMetaStack()
    val damageProcessorStack = DamageProcessorStack()

    val metadata = ConcurrentHashMap<String, Any>()

    fun initAttribute(engine: BukkitAttributeEngine) {
        attribute = AttributeSession(this, engine)

        val living = owner as? LivingEntity ?: return
        EquipmentPipeline.refresh(living)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getMetadata(key: String): T? {
        return metadata[key] as? T
    }

    fun refresh() {
        attribute.refreshDefaultAttributes()
    }

    fun close() {
        combatState?.leave()
        if (this::attribute.isInitialized) {
            EquipmentPipeline.cancel(ownerUUID)
            attribute.close()
        }
    }

}
