package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.common.base.Status

// 供 processor 在执行属性时对快照做临时更改
interface AttributeContainer {

    fun addAttribute(
        attributeId: String,
        captureId: String,
        value: Any,
        sourceId: String = "processor:temporary",
        priority: Int? = null
    )

    fun addAttribute(
        attributeId: String,
        status: Status<Any>,
        sourceId: String = "processor:temporary",
        priority: Int? = null
    )

    fun <T : Any> getValue(attributeId: String, captureId: String): T?

    fun <T : Any> getValue(attributeId: String, captureId: String, def: T): T = getValue(attributeId, captureId) ?: def

    fun hasAttribute(attributeId: String): Boolean

}