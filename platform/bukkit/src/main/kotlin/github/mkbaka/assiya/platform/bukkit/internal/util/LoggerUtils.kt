package github.mkbaka.assiya.platform.bukkit.internal.util

import github.mkbaka.assiya.platform.bukkit.AssiyaBukkitPlugin

import java.util.logging.Level

fun info(any: Any?) {
    AssiyaBukkitPlugin.INSTANCE.logger.info(any.toString())
}

fun warning(any: Any?) {
    AssiyaBukkitPlugin.INSTANCE.logger.warning(any.toString())
}

fun severe(any: Any?) {
    AssiyaBukkitPlugin.INSTANCE.logger.severe(any.toString())
}

fun logEx(throwable: Throwable, msg: String) {
    AssiyaBukkitPlugin.INSTANCE.logger.log(Level.SEVERE, msg, throwable)
}