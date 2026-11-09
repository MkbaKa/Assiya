package github.mkbaka.assiya.platform.bukkit.internal.util

fun hasClass(name: String): Boolean {
    return try {
        Class.forName(name)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}
