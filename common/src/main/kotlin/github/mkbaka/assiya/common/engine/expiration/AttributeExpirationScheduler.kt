package github.mkbaka.assiya.common.engine.expiration

import github.mkbaka.assiya.common.engine.AttributeEngineAction
import github.mkbaka.assiya.common.source.AttributeSourceLifecycle
import github.mkbaka.assiya.common.source.AttributeSourcePatch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AttributeExpirationScheduler(
    private val scope: CoroutineScope,
    private val channel: SendChannel<AttributeEngineAction>
) {

    // 属性源 -> 过期时间
    private val expireAts = linkedMapOf<String, Long>()

    private var timerJob: Job? = null
    private var scheduledExpireAt: Long? = null

    // 临时源是否变动
    private var dirty = false

    fun bind(patch: AttributeSourcePatch) {
        val lifecycle = patch.lifeCycle
        if (lifecycle is AttributeSourceLifecycle.Temporary) {
            expireAts[patch.sourceId] = System.currentTimeMillis() + lifecycle.duration.inWholeMilliseconds
            dirty = true
        } else {
            unbind(patch.sourceId)
        }
    }

    fun unbind(sourceId: String) {
        if (expireAts.remove(sourceId) != null) {
            dirty = true
        }
    }

    fun expiredSourceIds(): List<String> {
        timerJob?.cancel()
        timerJob = null
        scheduledExpireAt = null
        dirty = true

        val now = System.currentTimeMillis()
        val expired = arrayListOf<String>()
        for ((sourceId, expireAt) in expireAts) {
            if (expireAt <= now) {
                expired += sourceId
            }
        }
        return expired
    }

    fun reschedule() {
        if (!dirty) return
        dirty = false

        val next = expireAts.values.minOrNull()
        // 符合条件说明没有持续时间更短的临时源
        // 等最短源过期自己发 expire 到队列
        if (next == scheduledExpireAt && timerJob?.isActive == true) {
            return
        }

        // 走到这可能是 next 为 null   也就是不存在任何临时属性了
        // 或者 timerJob 莫名其妙噶了

        timerJob?.cancel()
        scheduledExpireAt = next
        // 如果没有其他临时属性就退出执行
        if (next == null) {
            timerJob = null
            return
        }

        val delayMillis = (next - System.currentTimeMillis()).coerceAtLeast(0L)
        // timerJob 只管持续时间最短的源
        timerJob = scope.launch {
            delay(delayMillis)
            channel.trySend(AttributeEngineAction.CheckExpiration)
        }
    }

}
