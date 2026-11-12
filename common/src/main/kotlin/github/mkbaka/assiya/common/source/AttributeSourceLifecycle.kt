package github.mkbaka.assiya.common.source

import kotlin.time.Duration

interface AttributeSourceLifecycle {

    object Stable : AttributeSourceLifecycle

    data class Temporary(val duration: Duration) : AttributeSourceLifecycle

    companion object {

        /**
         * 给 java/groovy 用
         * 直接用 Stable 单例要写成 Stable.INSTANCE 挺丑的
         */
        @JvmField
        val Stable = AttributeSourceLifecycle.Stable

    }

}