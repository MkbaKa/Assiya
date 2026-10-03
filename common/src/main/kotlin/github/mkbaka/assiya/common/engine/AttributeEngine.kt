package github.mkbaka.assiya.common.engine

import github.mkbaka.assiya.common.engine.snapshot.AttributeEngineSnapshot

import java.lang.AutoCloseable

/**
 * 管理属性数据的 "引擎"
 * 每个实体持有他自己的引擎
 */
interface AttributeEngine : AutoCloseable {

    /**
     * 推送命令
     */
    fun push(vararg action: AttributeEngineAction)

    /**
     * 掏个只读快照出来
     *
     * @return [AttributeEngineSnapshot]
     */
    fun snapshot(): AttributeEngineSnapshot

}