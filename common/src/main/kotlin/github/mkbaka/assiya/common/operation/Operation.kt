package github.mkbaka.assiya.common.operation

/**
 * 两个值的合并行为
 */
fun interface Operation<T : Any> {

    /**
     * current 为 null 说明这是第一个值 应直接返回
     *
     * @param [current] 当前值
     * @param [incoming] 合并目标
     * @return [T]
     */
    fun merge(current: T?, incoming: T): T

}