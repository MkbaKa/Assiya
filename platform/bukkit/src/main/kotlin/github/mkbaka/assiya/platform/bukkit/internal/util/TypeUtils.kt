package github.mkbaka.assiya.platform.bukkit.internal.util

object TypeUtils {

    fun Any.ofDouble(): Double {
        if (this is Double) return toDouble()

        return toString().toDouble()
    }

}