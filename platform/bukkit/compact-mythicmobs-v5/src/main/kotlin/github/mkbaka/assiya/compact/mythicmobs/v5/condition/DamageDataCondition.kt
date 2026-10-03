package github.mkbaka.assiya.compact.mythicmobs.v5.condition

import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.internal.damage.DamageProcessorStack
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.core.skills.SkillCondition

@MythicCondition("assiya:check-damage-data", "assiya:check-data", "assiya:checkdamagedata", "assiya:checkdata")
class DamageDataCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    private val key = mlc.getPlaceholderString(arrayOf("key", "k"), "")
    private val value = mlc.getPlaceholderString(arrayOf("value", "v"), "")
    private val useScript = mlc.getBoolean(arrayOf("useScript", "script", "us"), false)

    override fun check(ae: AbstractEntity): Boolean {
        val processor = DamageProcessorStack.getCurrent(ae.uniqueId) ?: return false
        val key = key.get(ae)
        val value = value.get(ae)
        val data = processor[CustomDataComponent]?.get(key) ?: return false

        return if (useScript) {
            NashornEngineAdapter.eval("$data$value", emptyMap()) as? Boolean ?: false
        } else {
            data.toString() == value
        }
    }

}
