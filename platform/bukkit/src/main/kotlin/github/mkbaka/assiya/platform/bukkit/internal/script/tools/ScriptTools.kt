package github.mkbaka.assiya.platform.bukkit.internal.script.tools

import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
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
        return processor[ProjectileComponent.Key]?.shooter?.entity
            ?: processor[TriggerComponent.Key]?.entity
    }

}
