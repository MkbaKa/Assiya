package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs

import github.mkbaka.assiya.common.parser.AttributeParsedSource
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionContextImpl
import github.mkbaka.assiya.platform.bukkit.internal.config.Config
import github.mkbaka.bukkitksp.annotation.Awake
import github.mkbaka.bukkitksp.annotation.CustomTask
import github.mkbaka.bukkitksp.annotation.LifeCycle
import org.bukkit.entity.Entity
import java.util.concurrent.ConcurrentHashMap

object MythicMobAttributes {

    private const val SOURCE_ID = "assiya:mythic-mobs:attributes"

    @Volatile
    private var levelAttributes = ConcurrentHashMap<String, LevelAttribute>()

    fun apply(entity: Entity, mobId: String, level: Double, attributes: List<String>, calculator: Boolean) {
        val session = AssiyaAPI.getSessionOrCreate(entity)
        if (attributes.isEmpty()) return

        val levelAttribute = levelAttributes.computeIfAbsent(mobId) {
            LevelAttribute(MobAttributeTemplate(mobId, attributes, calculator))
        }
        val parsed = levelAttribute.parsedByLevel.computeIfAbsent(level) {
            AssiyaAPI.parse(levelAttribute.template.render(level))
        }

        val values = AssiyaAPI.evaluate(entity, parsed, ConditionContextImpl(sourceId = SOURCE_ID))
        val patch = AttributeSourcePatch(
            sourceId = SOURCE_ID,
            priority = Config.global.MYTHICMOBS_SOURCE_PRIORITY,
            lifeCycle = AttributeSourceLifecycle.Stable,
            attributes = values
        )

        AssiyaAPI.addAttribute(session, true, patch)
    }

    @Awake(lifeCycle = LifeCycle.DISABLE)
    @CustomTask(taskId = "reload", priority = 114514)
    fun clear() {
        levelAttributes = ConcurrentHashMap()
    }

    private class LevelAttribute(val template: MobAttributeTemplate) {

        val parsedByLevel = ConcurrentHashMap<Double, AttributeParsedSource>()

    }

}
