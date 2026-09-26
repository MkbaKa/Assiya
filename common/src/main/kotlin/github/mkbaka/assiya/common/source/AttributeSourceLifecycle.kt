package github.mkbaka.assiya.common.source

import kotlin.time.Duration

interface AttributeSourceLifecycle {

    object Stable : AttributeSourceLifecycle

    data class Temporary(val duration: Duration) : AttributeSourceLifecycle

}