package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.CustomFilterComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.HandlerComponent
import github.mkbaka.assiya.platform.bukkit.api.attribute.trigger.TriggerSettings
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry.begin
import github.mkbaka.assiya.platform.bukkit.internal.registry.HandlerRegistry.complete
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Predicate

object HandlerRegistry : NamedRegistry<HandlerComponent>("handler") {

    /**
     * 根据 priority 排序后的 subscriber 列表
     * 方便触发的时候直接遍历
     */
    private val indexes = hashMapOf<String, MutableList<Subscriber>>()

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
            for (subscriber in subscribers) {
                addIndex(subscriber)
            }
        }
    }

    override fun unregister(id: String, value: HandlerComponent): Boolean {
        if (!super.unregister(id, value)) {
            return false
        }

        val subscribers = registrations.remove(id.lowercase()) ?: return true
        if (index.get()) {
            for (subscriber in subscribers) {
                removeIndex(subscriber)
            }
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
        indexes.clear()

        // 塞进实际触发类型对应的 list
        registrations.values.asSequence()
            .flatten()
            .groupByTo(indexes) { it.indexId }

        // 挨个类型重排
        for (subscribers in indexes.values) {
            subscribers.sortWith(SUBSCRIBER_COMPARATOR)
        }

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
        processor: Processor,
    ) {
        val subscribers = indexes[id.lowercase()] ?: return
        publish(subscribers, processor)
    }

    @JvmStatic
    fun publish(subscribers: List<Subscriber>, processor: Processor) {
        for (index in subscribers.indices) {
            val subscriber = subscribers[index]
            // 属性直接阻止了整个 processor 触发
            if (processor[CancellableComponent.Key]?.isCancelled == true) {
                break
            }

            // 阻止单个属性
            if (processor[CancellableAttributeComponent.Key]
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

    private fun addIndex(subscriber: Subscriber) {
        val subscribers = indexes.getOrPut(subscriber.indexId) { arrayListOf() }
        // 二分永远找不到即将新增的元素 所以这里一定会返回负数
        // 取绝对值再 -1 就能得出正确的下标位置
        val index = -subscribers.binarySearch(subscriber, SUBSCRIBER_COMPARATOR) - 1
        subscribers.add(index, subscriber)
    }

    private fun removeIndex(subscriber: Subscriber) {
        val subscribers = indexes[subscriber.indexId] ?: return
        val position = subscribers.binarySearch(subscriber, SUBSCRIBER_COMPARATOR)
        if (position >= 0) {
            subscribers.removeAt(position)
        }
        if (subscribers.isEmpty()) {
            indexes.remove(subscriber.indexId)
        }
    }

    class Subscriber(
        val indexId: String,
        val attributeId: String,
        val settings: TriggerSettings,
        val hasAttribute: BiPredicate<Processor, String>?,
        val action: Consumer<Processor>,
    ) {

        val attribute: Attribute?
            get() = AttributeRegistry[attributeId]

    }

}
