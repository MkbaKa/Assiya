# 已实现的组件

| 组件名                             | 提供的能力                                           |
|:--------------------------------|:------------------------------------------------|
| `CallbackComponent`             | 提供 Processor 执行结束后的回调机制                         |
| `CancellableAttributeComponent` | 处理可取消的属性相关逻辑                                    |
| `CancellableComponent`          | 提供基础的取消控制能力                                     |
| `CustomDataComponent`           | 存储与读取自定义扩展数据                                    |
| `DamageComponent`               | 携带伤害 ID 与伤害数值，用于伤害处理                            |
| `DamageMultipleComponent`       | 更进一步控制伤害细节                                      |
| `ProjectileComponent`           | 处理与弹射物（Projectile）相关的属性或逻辑                      |
| `TargetComponent`               | 定义目标实体的通用接口，便于 Processor 统一调用                   |
| `TargetDefaultComponent`        | 默认的目标实体实现                                       |
| `TargetLazyComponent`           | 提供目标实体的懒加载实现                                    |
| `TriggerComponent`              | 基础触发器组件                                         |
| `TriggerByEventComponent`       | 基于 EntityDamageByEntityEvent 事件触发的 processor 数据 |
| `TriggerByKillComponent`        | 基于 EntityDeathEvent 事件触发的 processor 数据          |
| `TriggerByMythicSkillComponent` | 基于 MythicMobs 技能触发的 processor 数据(v4 和 v5 是俩实现，不要搞混)         |
