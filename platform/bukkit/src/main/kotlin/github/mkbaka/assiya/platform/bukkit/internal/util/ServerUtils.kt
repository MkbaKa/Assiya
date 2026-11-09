package github.mkbaka.assiya.platform.bukkit.internal.util

val isPaper by lazy {
    hasClass("com.destroystokyo.paper.PaperConfig")
        || hasClass("io.papermc.paper.configuration.Configuration")
}