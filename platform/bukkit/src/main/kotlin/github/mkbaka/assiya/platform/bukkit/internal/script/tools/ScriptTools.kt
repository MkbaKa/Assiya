package github.mkbaka.assiya.platform.bukkit.internal.script.tools

import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import github.mkbaka.assiya.platform.bukkit.api.handler.component.ProjectileComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.TriggerComponent
import org.bukkit.entity.Entity
import java.util.concurrent.ThreadLocalRandom

object ScriptTools {

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