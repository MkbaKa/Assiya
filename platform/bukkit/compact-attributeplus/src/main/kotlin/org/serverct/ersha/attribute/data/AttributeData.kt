package org.serverct.ersha.attribute.data

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.serverct.ersha.attribute.data.counter.AttributeCounter

class AttributeData(
    val entity: Entity
) {
    val counter = AttributeCounter(entity)

    val sourceEntity: Entity get() = entity

    fun getPlayer(): Player? = Bukkit.getPlayer(entity.uniqueId)

    fun containsSource(name: String): Boolean = name in AssiyaAPI.getSnapshot(entity).sources

    fun getRandomValue(attributeName: String): Number {
        val id = AssiyaAPI.getAttribute(attributeName)?.id ?: attributeName
        return DefaultAttributeContainer(AssiyaAPI.getSnapshot(entity)).getValue<Number>(id, "total") ?: 0.0
    }

    fun getAttributeValue(attributeName: String): Array<Number> =
        throw UnsupportedOperationException("Attribute ranges are not supported. Use getRandomValue instead.")

    fun takeApiAttribute(source: String) = AssiyaAPI.takeAttribute(entity, source)

    @JvmOverloads
    fun updateAttribute(async: Boolean = false) {
        val target = entity as? LivingEntity ?: return
        AssiyaAPI.refreshEquipment(target)
    }
}
