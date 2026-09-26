package github.mkbaka.assiya.platform.bukkit.internal.util

val isPaper by lazy {
    hasClass("com.destroystokyo.paper.PaperConfig")
        || hasClass("io.papermc.paper.configuration.Configuration")
}

private fun hasClass(name: String): Boolean {
    return try {
        Class.forName(name)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}
