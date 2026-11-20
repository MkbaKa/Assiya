package org.serverct.ersha.api

import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.EntityTaskAPI
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.serverct.ersha.attribute.data.AttributeData

class AttributeAPI {

    companion object {

        private const val SCRIPT_TASK = "attributeplus:callback"

        init {
            EntityTaskAPI.registerTask(SCRIPT_TASK, { owner, params ->
                if (params["skip-state-check"] == true || !owner.isDead) {
                    (params["function"] as Runnable).run()
                }
            })
        }

        @JvmStatic
        fun getAttrData(entity: LivingEntity): AttributeData = AttributeData(entity)

        @JvmStatic
        fun addSourceAttribute(data: AttributeData, source: String, itemStack: ItemStack) {
            val session = AssiyaAPI.getSession(data.entity) ?: return
            AssiyaAPI.addAttribute(session, source, itemStack, 2026, AttributeSourceLifecycle.Stable)
        }

        @JvmStatic
        fun addSourceAttribute(data: AttributeData, source: String, list: List<String>) {
            val session = AssiyaAPI.getSession(data.entity) ?: return
            AssiyaAPI.addAttribute(session, source, list, 2026, AttributeSourceLifecycle.Stable)
        }

        @JvmStatic
        fun addSourceAttribute(data: AttributeData, source: String, attribute: HashMap<String, Array<Number>>) {
            addSourceAttribute(data, source, attribute.map { (name, values) -> "$name: ${values[0]}-${values[1]}" })
        }

        @JvmStatic
        fun addSourceAttributeFromItems(data: AttributeData, source: String, items: List<ItemStack>) {
            val session = AssiyaAPI.getSession(data.entity) ?: return
            AssiyaAPI.addAttributeItems(session, source, items, 2026, AttributeSourceLifecycle.Stable)
        }

        @JvmStatic
        fun takeSourceAttribute(data: AttributeData, source: String) {
            AssiyaAPI.takeAttribute(data.entity, source)
        }

        @JvmStatic
        fun attackTo(entity: LivingEntity, attacker: LivingEntity, damage: Double) {
            AssiyaAPI.attackTo(attacker, entity, damage)
        }

        @JvmStatic
        fun addStaticAttributeSource(data: AttributeData, source: String, list: List<String>) = addSourceAttribute(data, source, list)

        @JvmStatic
        fun runEntityTask(delay: Long, name: String, entity: LivingEntity, skipStateCheck: Boolean, function: Runnable) {
            EntityTaskAPI.runTask(entity, SCRIPT_TASK) { options ->
                options.key("attributeplus:entity:$name")
                    .delay(((delay + 49) / 50).coerceAtLeast(1))
                    .params(mapOf("skip-state-check" to skipStateCheck, "function" to function))
            }
        }

    }

}
