package github.mkbaka.assiya.platform.bukkit.internal.script.tools

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.attribute.component.OptionsComponent
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TargetComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.colored
import github.mkbaka.assiya.platform.bukkit.internal.util.StringUtils.replaceVariables
import org.bukkit.entity.Entity
import java.util.concurrent.ThreadLocalRandom
import java.util.function.BiConsumer

object ScriptTools {

    @JvmStatic
    fun loadConfig(path: String, defaults: Map<String, *>): ConfigNode = ConfigLoader.loadConfig(path, defaults)

    @JvmStatic
    fun loadConfigs(
        path: String,
        defaultFile: String,
        defaults: Map<String, *>,
        processor: BiConsumer<String, ConfigNode>
    ) {
        ConfigLoader.load(path, defaultFile, defaults, processor)
    }

    @JvmStatic
    fun random(): Double = ThreadLocalRandom.current().nextDouble()

    @JvmStatic
    fun random(min: Double, max: Double): Double = ThreadLocalRandom.current().nextDouble(min, max)

    @JvmStatic
    fun chance(target: Double): Boolean = random() <= (target / 100)

    @JvmStatic
    fun getAttacker(processor: Processor): Entity? {
        return processor[ProjectileComponent]?.shooter?.entity
            ?: processor[TriggerComponent]?.entity
    }

    @JvmStatic
    fun sendMessage(entity: Entity, message: String, variables: Map<String, String>) {
        entity.sendMessage(
            message.replaceVariables(variables).colored()
        )
    }

    @JvmStatic
    @JvmOverloads
    fun sendCombatMessage(
        attribute: Attribute,
        processor: Processor,
        variables: Map<String, Any> = emptyMap()
    ) {
        val messages = attribute[OptionsComponent] ?: return

        val attackerMessage = messages.getString("attacker")
        val entityMessage = messages.getString("entity")

        val attacker = getAttacker(processor)
        val entity = processor[TargetComponent]?.entity

        val vars = hashMapOf<String, String>()
        if (attacker != null) {
            vars["attacker"] = attacker.name
        }
        if (entity != null) {
            vars["entity"] = entity.name
        }

        vars.putAll(variables.mapValues { it.value.toString() })

        if (attacker != null && !attackerMessage.isNullOrEmpty()) {
            sendMessage(attacker, attackerMessage, vars)
        }
        if (entity != null && !entityMessage.isNullOrEmpty()) {
            sendMessage(entity, entityMessage, vars)
        }
    }

}
