package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.CustomFilterComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.HandlerComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerSettings
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Predicate

object HandlerRegistry : NamedRegistry<HandlerComponent>("handler") {

    /**
     * 根据 priority 排序后的 subscriber 列表
     * 方便触发的时候直接遍历
     */
    @Volatile
    private var indexes: Map<String, List<Subscriber>> = emptyMap()

    /**
     * 属性持有的 subscriber
     */
    private val registrations = hashMapOf<String, List<Subscriber>>()

    /**
     * 是否对单个 handler 的注册做索引构建
     * reload 的时候会关掉 避免每条属性加载都酷酷对 list 排序
     * 鉴于大部分注册行为实际上是在插件加载时发生的 所以这部分属性可以拖到配置首次加载后一起排序
     * 见 [begin] [complete]
     */
    private var index = AtomicBoolean(false)

    private val SUBSCRIBER_COMPARATOR = compareBy<Subscriber>(
        { it.settings.priority },
        { it.attributeId }
    )

    override fun register(id: String, value: HandlerComponent) {
        super.register(id, value)

        val subscribers = value.handlers.map { (trigger, handler) ->
            Subscriber(
                indexId = trigger.indexId.lowercase(),
                attributeId = id,
                settings = handler.settings,
                hasAttribute = TriggerRegistry[trigger.id]?.hasAttribute,
                action = handler.action,
            )
        }
        // 方便 unregister
        registrations[id.lowercase()] = subscribers

        if (index.get()) {
            addIndexes(subscribers)
        }
    }

    override fun unregister(id: String, value: HandlerComponent): Boolean {
        if (!super.unregister(id, value)) {
            return false
        }

        val subscribers = registrations.remove(id.lowercase()) ?: return true
        if (index.get()) {
            removeIndexes(subscribers)
        }
        return true
    }

    /**
     * 批量注册前关掉单元素排序
     */
    @JvmStatic
    fun begin() {
        index.set(false)
    }

    /**
     * 批量注册完统一排序
     */
    @JvmStatic
    fun complete() {
        val rebuilt = registrations.values.asSequence()
            .flatten()
            .groupBy { it.indexId }
            .mapValues { (_, subscribers) -> subscribers.sortedWith(SUBSCRIBER_COMPARATOR) }
        indexes = rebuilt
        index.set(true)
    }

    @JvmStatic
    fun select(id: String, filter: Predicate<Subscriber>): List<Subscriber> {
        val subscribers = indexes[id.lowercase()] ?: return emptyList()
        val selected = arrayListOf<Subscriber>()
        for (subscriber in subscribers) {
            if (filter.test(subscriber)) selected += subscriber
        }
        return selected
    }

    /**
     * 触发对应类型的 handler
     *
     * @param [id] 类型
     * @param [processor] 上下文
     */
    @JvmStatic
    fun publish(
        id: String,
        processor: BukkitProcessor,
    ) {
        val subscribers = indexes[id.lowercase()] ?: return
        publish(subscribers, processor)
    }

    @JvmStatic
    fun publish(subscribers: List<Subscriber>, processor: BukkitProcessor) {
        for (index in subscribers.indices) {
            val subscriber = subscribers[index]
            // 属性直接阻止了整个 processor 触发
            if (processor[CancellableComponent]?.isCancelled == true && !subscriber.settings.ignoreCancelled) {
                continue
            }

            // 阻止单个属性
            if (processor[CancellableAttributeComponent]
                    ?.isCancelled(subscriber.attributeId) == true
            ) {
                continue
            }

            // 属性值判断
            if (!subscriber.settings.alwaysRun) {
                val hasAttribute = subscriber.hasAttribute

                if (hasAttribute?.test(processor, subscriber.attributeId) == false) {
                    continue
                }
            }

            if (subscriber.attribute?.get(CustomFilterComponent.Key)?.test(processor) == false) {
                continue
            }

            // 执行逻辑
            subscriber.action.accept(processor)
        }
    }

    private fun addIndexes(subscribers: List<Subscriber>) {
        if (subscribers.isEmpty()) return
        val updated = indexes.toMutableMap()
        for ((indexId, additions) in subscribers.groupBy { it.indexId }) {
            val indexed = updated[indexId]?.toMutableList() ?: arrayListOf()
            for (subscriber in additions) {
                val position = -indexed.binarySearch(subscriber, SUBSCRIBER_COMPARATOR) - 1
                indexed.add(position, subscriber)
            }
            updated[indexId] = indexed
        }
        indexes = updated
    }

    private fun removeIndexes(subscribers: List<Subscriber>) {
        if (subscribers.isEmpty()) return
        val updated = indexes.toMutableMap()
        for ((indexId, removals) in subscribers.groupBy { it.indexId }) {
            val indexed = updated[indexId]?.toMutableList() ?: continue
            for (subscriber in removals) {
                val position = indexed.binarySearch(subscriber, SUBSCRIBER_COMPARATOR)
                if (position >= 0) {
                    indexed.removeAt(position)
                }
            }
            if (indexed.isEmpty()) {
                updated.remove(indexId)
            } else {
                updated[indexId] = indexed
            }
        }
        indexes = updated
    }

    class Subscriber(
        val indexId: String,
        val attributeId: String,
        val settings: TriggerSettings,
        val hasAttribute: BiPredicate<BukkitProcessor, String>?,
        val action: Consumer<BukkitProcessor>,
    ) {

        val attribute: Attribute?
            get() = AttributeRegistry[attributeId]

    }

}
