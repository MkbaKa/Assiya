package github.mkbaka.assiya.platform.bukkit.internal.hook.mythicmobs.v5.condition

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.ReaderComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.core.skills.SkillCondition
import io.lumine.mythic.api.skills.conditions.IEntityCondition

@MythicCondition("assiya:attribute-value", "assiya:attributevalue")
class AttributeValueCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    private val attribute = mlc.getPlaceholderString(arrayOf("name", "n", "attribute", "attr"), "")
    private val capture = mlc.getPlaceholderString(arrayOf("capture", "c"), "total")
    private val expression = mlc.getPlaceholderString(arrayOf("action", "a", "value", "v"), ">0")

    override fun check(ae: AbstractEntity): Boolean {
        val entity = ae.bukkitEntity
        val attributeKey = attribute.get(ae)
        val captureId = capture.get(ae)
        val action = expression.get(ae)

        val attribute = AssiyaAPI.getAttribute(attributeKey) ?: error("Unknown attribute: $attributeKey")
        val attributeId = attribute.id
        val reader = attribute[ReaderComponent]?.reader ?: error("$attributeId has no reader")
        require(captureId in reader.capture || captureId in reader.placeholders) {
            "Unknown attribute value: $attributeId:$captureId"
        }
        val status = AssiyaAPI.getSnapshot(entity)[attributeId] ?: Status.empty<Any>()
        val value = reader.read(status, captureId) ?: return false
        return NashornEngineAdapter.eval("$value$action", emptyMap()) as? Boolean ?: false
    }

}
