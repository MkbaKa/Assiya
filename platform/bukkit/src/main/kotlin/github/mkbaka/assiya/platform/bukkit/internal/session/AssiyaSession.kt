package github.mkbaka.assiya.platform.bukkit.internal.session

import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class AssiyaSession(
    entity: Entity
) {

    private val entityRef = WeakReference(entity)

    val owner: Entity?
        get() = entityRef.get()

    val ownerUUID = entity.uniqueId

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
        if (this::attribute.isInitialized) {
            EquipmentPipeline.cancel(ownerUUID)
            attribute.close()
        }
    }

}