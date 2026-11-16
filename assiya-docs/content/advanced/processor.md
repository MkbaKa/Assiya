# Processor

Processor 是一个抽象概念，在 Assiya 中代表一次任务的 `过程`。

其不持有任何能力、行为，所有能力均依赖 `Component` 即 [组件](#如何改进) 来实现。

这使得 Processor 有极高的拓展性，不需要修改 Assiya 本身的代码也能实现很多奇妙的功能。

## 一个典型使用场景

`DamageProcessor` 也就是 `attack` 与 `defense` 所持有的过程实现。

在传统的对象结构中，一次伤害过程能让消费处获得什么信息都是固定的，以我几年前写的插件 `Atziluth` 为例：

::: details 展开以阅读代码
```kotlin :line-numbers
/**
 * 处理战斗过程的数据
 */
interface FightData {

    /**
     * 攻击者
     */
    val attacker: LivingEntity

    /**
     * 受击者
     */
    var entity: LivingEntity?

    /**
     * 触发此次伤害的抛射物
     * 不存在时为null
     */
    var projectile: Projectile?

    /**
     * 要触发的属性列表
     */
    val attributes: MutableList<CustomAttribute>

    /**
     * 存储战斗过程中的属性数据
     */
    val attributeData: MutableMap<UUID, MutableMap<String, DoubleArray>>

    /**
     * 仅此次处理中可用的临时数据
     */
    val tempData: MutableMap<UUID, EntityData>

    /**
     * 伤害事件
     */
    val event: EntityDamageByEntityEvent

    /**
     * 伤害值
     */
    var damageValue: Double

    /**
     * 蓄力程度 (0 ~ 1)
     */
    var force: Double

    /**
     * 取消此次伤害处理
     * 并取消事件执行
     */
    var isCancelled: Boolean

    /**
     * 此次事件是否由远程伤害触发
     * @return [Boolean]
     */
    fun isProjectileDamage(): Boolean

    /**
     * 存储数据
     * 仅此次处理中可用
     * @param [uuid] 数据归属者的uuid
     * @param [key] key
     * @param [value] value
     */
    fun saveData(uuid: UUID, key: String, value: Any)

    /**
     * 获取数据
     * @param [uuid] 数据归属者的uuid
     * @param [key] key
     * @return [Any?]
     */
    fun getData(uuid: UUID, key: String): Any?

    /**
     * 获取此次攻击的蓄力进度
     * 0 ~ 1
     * 仅对玩家生效
     * @return [Double]
     */
    fun getFore(): Double

    /**
     * 增加此次伤害
     * @param [value] 伤害值
     */
    fun addDamage(value: Double)

    /**
     * 减少此次伤害
     * @param [value] 伤害值
     */
    fun takeDamage(value: Double)

    /**
     * 设置此次伤害
     * @param [value] 伤害值
     */
    fun setDamage(value: Double)

    /**
     * 获取此次伤害
     * @return [Double] 伤害值
     */
    fun getDamage(): Double

    /**
     * 操作当前事件触发时的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [value] 属性值
     */
    fun addAttribute(entity: LivingEntity, name: String, value: Double)

    /**
     * 操作当前事件触发时的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [values] 属性值
     */
    fun addAttribute(entity: LivingEntity, name: String, values: DoubleArray)

    /**
     * 操作当前事件触发时的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [value] 属性值
     */
    fun setAttribute(entity: LivingEntity, name: String, value: Double)

    /**
     * 操作当前事件触发时的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [values] 属性值
     */
    fun setAttribute(entity: LivingEntity, name: String, values: DoubleArray)

    /**
     * 操作当前事件触发时的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [value] 属性值
     */
    fun takeAttribute(entity: LivingEntity, name: String, value: Double)

    /**
     * 操作当前事件触发时的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [values] 属性值
     */
    fun takeAttribute(entity: LivingEntity, name: String, values: DoubleArray)

    /**
     * 获取战斗数据中的属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @param [valueType] 属性值类型
     * @return [Double]
     */
    fun getAttribute(
        entity: LivingEntity,
        name: String,
        valueType: AttributeValueType = AttributeValueType.RANDOM
    ): Double

    /**
     * 处理攻击和防御类属性
     * 每个属性计算后的伤害值会反馈到damageValue
     * 若调用cancelCalculate()则会取消此次伤害处理但不取消原始伤害事件
     * 若调用isCancelled=true则会取消此次伤害处理并取消原始伤害事件
     */
    fun handle()

    /**
     * 尝试运行指定属性
     * 仅对 Atziluth 属性生效
     * * values内的值 会 附加给该属性
     * * 触发后删除
     * @param [attrName] 属性名
     * @param [values] 数值
     */
    fun callAttribute(attrName: String, values: DoubleArray)

    /**
     * 尝试运行指定属性
     * 仅对 Atziluth 属性生效
     * * values内的值 不会 附加给该属性
     * @param [attr] 属性实例
     * @param [values] 数值
     */
    fun callAttribute(attr: CustomAttribute, values: DoubleArray)

    /**
     * 获取属性值
     * @param [entity] 实体
     * @param [name] 属性名
     * @return [DoubleArray?]
     */
    fun getAttributeValues(entity: LivingEntity, name: String): DoubleArray?

    /**
     * 获取随机值 (minValue ~ maxValue)
     * @param [entity] 实体
     * @param [name] 属性名
     * @return [Double]
     */
    fun getRandomValue(entity: LivingEntity, name: String): Double

    /**
     *
     * 取消此次伤害处理不取消事件执行
     */
    fun cancelCalculate()

    /**
     * 伤害处理是否被取消
     */
    fun isCancelCal(): Boolean

    /**
     * 取消指定属性执行
     * 无法取消在该属性之前执行过的属性
     *
     * @param [entity] 实体
     * @param [attr] 属性名
     */
    fun cancelAttribute(entity: LivingEntity, attr: String)

    fun cancelledAttributes(): Map<UUID, Set<CustomAttribute>>

    /**
     * 初始化实体属性
     */
    fun initEntityAttributeValues(entity: LivingEntity)

    /**
     * 初始化需要执行的属性
     */
    fun initRunnableAttributes(attrs: Collection<CustomAttribute>)

    /**
     * 初始化需要执行的属性
     */
    fun initRunnableAttributesByDefault()

    fun hasEvent(): Boolean

}
```
:::

可以看到，`FightData` 为了提供尽可能多的信息，将两百多行代码压在了一个接口中。

这不仅会给想要查询方法的用户带来极大的阅读负担，也将可访问的内容、提供的能力限定在了一个接口中。

也许它足以满足 90% 的需求，但我认为它不是我想要实现的东西。

## 如何改进的？

我将一个个不同的功能分类，拆分成了许多组件实现，比如 `save/get data` 对应的 `CustomDataComponent`：

```kotlin :line-numbers
/**
 * 允许在 processor 执行过程中跨属性传递数据
 */
class CustomDataComponent(
    private val delegate: MutableMap<String, Any?> = hashMapOf()
) : ProcessorComponent, MutableMap<String, Any?> by delegate, ForkableComponent<CustomDataComponent> {

    override val key: ProcessorComponent.Key<*> = Key

    operator fun <T> get(key: String, def: T) = delegate [key] as? T ?: def

    override fun fork(): CustomDataComponent = CustomDataComponent(HashMap(delegate))

    companion object Key : ProcessorComponent.Key<CustomDataComponent>

}
```

尽管实现它需要的代码行数远大于两个方法，但它把 `自定义数据` 这一行为与 `FightData` 解耦了出来，使得我们实现其他 `Processor` 时，不再需要把 `save/get data` 与对应的 `Map` 字段到处复制。

可以在构造 `Processor` 时直接赋予该组件：

```kotlin
DamageProcessor.of(attacker, defender) { pcb ->
    pcb += CustomDataComponent()
}
```

这里的 `pcb` 即 `ProcessorComponentBuilder`，用于构造不可变的组件列表。

## 为什么不直接使用 Map<Key, Object> 存所有数据？

很显然，答案就在问题上，`Object` 类型只能满足任意拓展的需求，但访问时要享大福了，到处转换类型：

```kotlin
// Map<String, Any>
val map = processor.getDataMap()

// ...
val player = map["xxx"] as Player
val target = map["xxx"] as LivingEntity
val damage = map["xxx"] as Double
```

而且 `Map` 的 `key` 显然只有在存入它的地方能够找到，这个阅读负担远比 `FightData` 恐怖的多。

也许你会反驳我可以将所有 `key` 都放在一个类的常量字段中，如 `map[FightConst.PLAYER]`、`map[FightConst.TARGET]`、`map[FightConst.DAMAGE]`。

这样做确实解决了阅读困难这个问题~~（也许吧）~~，但类型转换问题并没有消除。

不如看看组件是怎么解决的吧：

```kotlin
val player = processor[PlayerComponent.Key].player
val target = processor[TargetComponent.Key].entity
val damage = processor[DamageComponent.Key].value
```

尽管 `processor` 只能访问出组件实例本身，还要单独调用获取 `player` 字段，但 `Key` 本身就是你想获取数据来源的成员。

你想获得玩家，直接尝试通过 IDE 补全 `PlayerComponent`。想获得目标、伤害也一样，直接去找对应的组件就好了。~~(虽然现在可能已经没人会手写代码了)~~

而这只是它带来的附赠反馈，组件化真正的优点在于你可以把任何数据、行为都放在特定组件实现中。

## 自由度

上文主要体现了组件化能够做到哪些事，而自由度主要体现在 `DamageProcessor` 的实现中：

```kotlin
/**
 * 一次伤害处理的过程
 * 可以通过 [HandlerRegistry.select] 提前筛出允许执行的 subscriber
 */
class DamageProcessor(
    override val components: Map<ProcessorComponent.Key<*>, ProcessorComponent>,
    val subscribers: List<Subscriber>? = null
) : BukkitProcessor {

    constructor(pcb: Consumer<ProcessorComponentBuilder>) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build()
    )

    constructor(
        subscribers: List<Subscriber>?,
        pcb: Consumer<ProcessorComponentBuilder>
    ) : this(
        ProcessorComponentBuilder().apply(pcb::accept).build(),
        subscribers
    )

    override fun process() {
        val selected = subscribers
        if (selected == null) {
            HandlerRegistry.publish("damage", this)
        } else {
            HandlerRegistry.publish(selected, this)
        }
        this[CallbackComponent]?.invoke(this)
    }

    /**
     * 基于当前组件数据创建新的 BukkitProcessor
     *
     * 只有实现 [ForkableComponent] 接口的才参与创建
     */
    @JvmOverloads
    fun fork(builder: Consumer<ProcessorComponentBuilder>? = null): DamageProcessor {
        val pcb = ProcessorComponentBuilder()

        for (component in components.values) {
            val forkable = component as? ForkableComponent<*> ?: continue
            pcb += forkable.fork()
        }

        builder?.accept(pcb)

        return DamageProcessor(pcb.build(), subscribers)
    }

    companion object {

        @JvmStatic
        fun of(
            attacker: Entity,
            defender: LivingEntity,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return ofSnapshot(attacker, defender, builder = builder)
        }

        @JvmStatic
        @JvmOverloads
        fun ofSnapshot(
            attacker: Entity,
            defender: LivingEntity,
            subscribers: List<Subscriber>? = null,
            attackerSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(attacker),
            defenderSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(defender),
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageProcessor {
            return DamageProcessor(subscribers) { pcb ->
                pcb += BukkitProcessor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot)
                pcb += TargetDefaultComponent(defender, defenderSnapshot)
                pcb += DamageMultipleComponent()
                builder?.accept(pcb)
            }
        }

        @JvmStatic
        fun ofProjectile(
            projectile: Projectile,
            defender: LivingEntity,
            shooter: Entity,
            force: Float,
            builder: Consumer<ProcessorComponentBuilder>?
        ): DamageProcessor {
            return ofSnapshot(projectile, defender) { pcb ->
                pcb += ProjectileComponent(
                    TriggerComponent(shooter, AssiyaAPI.getSnapshot(shooter)),
                    pcb[TriggerComponent]!!,
                    force
                )
                builder?.accept(pcb)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun ofLazyTarget(
            attacker: Entity,
            subscribers: List<Subscriber>? = null,
            attackerSnapshot: AttributeEngineSnapshot = AssiyaAPI.getSnapshot(attacker),
            builder: Consumer<ProcessorComponentBuilder>? = null
        ): DamageProcessor {
            return DamageProcessor(subscribers) { pcb ->
                pcb += BukkitProcessor.defaults()
                pcb += TriggerComponent(attacker, attackerSnapshot)
                pcb += TargetLazyComponent()
                pcb += DamageMultipleComponent()
                builder?.accept(pcb)
            }
        }

    }

}

```

可以看到，我们的 `DamageProcessor` 只有一个叫做 `process` 的入口方法，没有限定任何数据、能力，与 `FightData` 有显著区别。

但它仍然可以实现 `FightData` 的一切功能，而且它的未来远不止于此。

`DamageProcessor` 拥有的组件都是在工厂函数中传递的，它们先定义了一些基础能力组件，又把 `ProcessorComponentBuilder` 暴露给了回调方法，使得调用处可以在此基础上增加自己的新组件。

比如，[DamageMeta 技能](../mythicmobs/mechanic/damage-meta.md)在执行时就传入了一个叫做 `TriggerBySkillComponent` 的组件，供 `DamageProcessor` 的消费方简单粗暴的把这次伤害认定为**一次由技能唤起的伤害。**

而这是 `FightData` 这种传统结构远远无法实现的。

不过需要注意的是，像 `CallbackComponent` 这种逻辑能力类的组件，需要自行在特定时机调用，它并不会自动执行。比如 `process` 方法里的 `this[CallbackComponent]?.invoke(this)`