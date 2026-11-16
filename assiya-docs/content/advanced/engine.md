# 属性管理

调用 `AssiaAPI.addAttribute` 后，经过解析、条件判断的结果会交由一个叫做 `AttributeEngine` 的东西调度执行。

它是一个消息队列实现，负责把各个线程发来的属性操作压到一个消息处理线程执行，不需要依赖锁也能保证线程安全。

正常情况下，除 `Refresh` 以外的消息(即 `AttributeEngineAction`)只会不断累计在待处理列表中，直到 `Refresh` 消息被消费。

`Refresh` 就像一个闹钟，通知队列里的消息到点上班，执行它们对应的属性操作。

其大概时序如下：

```mermaid
sequenceDiagram
    participant A as 提交线程 A
    participant B as 提交线程 B
    participant E as AttributeEngine<br/>消息处理协程
    participant S as 只读快照
    participant R as 读取线程

    A-)E: UpdateSource（更新属性源）
    E->>E: 加入待处理列表
    B-)E: RemoveSource（移除属性源）
    E->>E: 加入待处理列表
    Note over E,S: 操作暂未执行，仍然提供上一次的快照
    R->>S: 读取属性值
    S-->>R: 上一次刷新后的结果

    A-)E: Refresh
    activate E
    E->>E: 取出并清空待处理列表
    loop 按列表顺序执行累计的操作
        E->>E: 更新或移除属性源
    end
    E->>E: 合并受影响的属性，生成最终结果
    E->>S: 发布新的 AttributeEngineSnapshot
    deactivate E

    R->>S: 读取属性值
    S-->>R: 本次刷新后的结果
    B-)E: UpdateSource（下一次属性操作）
    E->>E: 加入待处理列表，等待下一次 Refresh
```

当累计的命令消费完后，`AttributeEngine` 会根据这次影响的所有属性源/值计算出一份最终结果，也就是 `AttributeEngineSnapshot` 属性快照。

属性快照只提供无锁的属性值读取，不提供任何写操作，不会存在竞态条件。

这两者构成了一个完全无锁、足以覆盖大部分并发场景的属性值管理。
