package github.mkbaka.assiya.common.source

import github.mkbaka.assiya.common.base.Capture
import github.mkbaka.assiya.common.base.Status

data class AttributeValue<T : Any>(
    val attributeId: String,
    val status: Status<T>,
    val capture: Capture<T>,
)