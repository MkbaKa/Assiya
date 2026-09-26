package github.mkbaka.assiya.platform.bukkit.internal.script

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin

import github.mkbaka.assiya.platform.bukkit.internal.util.logEx
import java.util.logging.Logger

interface Script {

    val id: String

    fun hasFunction(function: String): Boolean

    fun invoke(function: String, vararg args: Any?): Any?

    fun invokeSafely(function: String, vararg args: Any?): Any? {
        return invokeSafely(AssiyaBukkitPlugin.INSTANCE.logger, function, *args)
    }

    fun invokeSafely(logger: Logger, function: String, vararg args: Any?): Any? {
        return try {
            invoke(function, *args)
//            measureTimedValue {
//                invoke(function, *args)
//            }.let {
//                println("invoke ${this::class.java.simpleName} from $id#$function used = ${it.duration.inWholeMicroseconds}us.")
//                it.value
//            }
        } catch (e: Throwable) {
            logEx(e, "Failed to execute script $id")
            null
        }
    }

}
