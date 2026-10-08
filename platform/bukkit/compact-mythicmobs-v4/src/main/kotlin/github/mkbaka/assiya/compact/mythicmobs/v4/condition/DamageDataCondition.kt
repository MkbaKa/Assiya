package github.mkbaka.assiya.compact.mythicmobs.v4.condition

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.internal.script.impl.NashornEngineAdapter
import github.mkbaka.mythicksp.annotation.MythicCondition
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillCondition
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition

@MythicCondition("assiya:check-damage-data", "assiya:check-data", "assiya:checkdamagedata", "assiya:checkdata")
class DamageDataCondition(mlc: MythicLineConfig) : SkillCondition(mlc.line), IEntityCondition {

    private val key = mlc.getPlaceholderString(arrayOf("key", "k"), "")
    private val value = mlc.getPlaceholderString(arrayOf("value", "v"), "")
    private val useScript = mlc.getBoolean(arrayOf("useScript", "script", "us"), false)

    override fun check(ae: AbstractEntity): Boolean {
        val session = AssiyaAPI.getSession(ae.uniqueId) ?: return false
        val processor = session.damageProcessorStack.current ?: return false
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
