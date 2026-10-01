package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.CustomTask
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.entity.Entity

object MythicMobAttributes {

    private const val SOURCE_ID = "Assiya:MythicMobs:Attributes"
    private const val SOURCE_PRIORITY = 100
    private val definitions = hashMapOf<String, LevelAttribute>()

    fun apply(entity: Entity, mobId: String, level: Double, attributes: List<String>, calculator: Boolean) {
        val session = AssiyaAPI.getSessionOrCreate(entity)
        if (attributes.isEmpty()) return

        val levelAttribute = definitions.computeIfAbsent(mobId) {
            LevelAttribute(MobAttributeTemplate(mobId, attributes, calculator))
        }
        val parsed = levelAttribute.parsedByLevel.computeIfAbsent(level) {
            AssiyaAPI.parseSync(levelAttribute.template.render(level))
        }

        val values = AssiyaAPI.evaluateSync(entity, parsed)

        session.submitPatches(
            listOf(
                AttributeSourcePatch(
                    sourceId = SOURCE_ID,
                    priority = SOURCE_PRIORITY,
                    lifeCycle = AttributeSourceLifecycle.Stable,
                    attributes = values
                )
            )
        )
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    @CustomTask(taskId = "reload", priority = 114514)
    fun clear() {
        definitions.clear()
    }

    private class LevelAttribute(val template: MobAttributeTemplate) {

        val parsedByLevel = hashMapOf<Double, AttributeParsedSource>()

    }

}
