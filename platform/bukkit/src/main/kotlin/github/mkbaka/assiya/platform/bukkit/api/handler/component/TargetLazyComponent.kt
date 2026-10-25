package github.mkbaka.assiya.platform.bukkit.api.handler.component

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.AttributeContainer
import github.mkbaka.assiya.platform.bukkit.api.handler.DefaultAttributeContainer
import org.bukkit.entity.LivingEntity

/**
 * 延迟设置实体的 [TargetComponent] 实现
 * 可以用来提前构造 DamageMeta 和 DamageProcessor
 * 要执行逻辑时再设置目标
 * 用来实现一些奇奇怪怪的效果?
 *
 * **必须在调用 [github.mkbaka.assiya.platform.bukkit.api.handler.Processor.process] 前调用 [set]**
 */
class TargetLazyComponent : TargetComponent {

    private lateinit var lazyEntity: LivingEntity

    private lateinit var lazySnapshot: AttributeEngineSnapshot

    private lateinit var lazyContainer: AttributeContainer

    override val entity: LivingEntity
        get() = lazyEntity

    override val snapshot: AttributeEngineSnapshot
        get() = lazySnapshot

    @JvmOverloads
    fun set(
        entity: LivingEntity,
        snapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(entity),
        container: AttributeContainer = DefaultAttributeContainer(snapshot)
    ) {
        require(!this::lazyEntity.isInitialized) {
            "entity has been already set"
        }

        this.lazyEntity = entity
        this.lazySnapshot = snapshot
        this.lazyContainer = container
    }

    override fun addAttribute(
        attributeId: String,
        captureId: String,
        value: Any,
        sourceId: String,
        priority: Int?
    ) {
        lazyContainer.addAttribute(attributeId, captureId, value, sourceId, priority)
    }

    override fun addAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String,
        priority: Int?
    ) {
        lazyContainer.addAttribute(attributeId, status, sourceId, priority)
    }

    override fun <T : Any> getValue(attributeId: String, captureId: String): T? {
        return lazyContainer.getValue(attributeId, captureId)
    }

    override fun hasAttribute(attributeId: String): Boolean {
        return lazyContainer.hasAttribute(attributeId)
    }

}
