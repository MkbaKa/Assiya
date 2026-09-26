package github.mkbaka.assiya.platform.bukkit.internal.hook.placeholderapi

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.function.BiFunction

class PlaceholderExpansionWrapper(
    val identifier: String,
    val author: String,
    val version: String,
    val handler: BiFunction<OfflinePlayer?, String, String>
) {

    private val expansion = object : PlaceholderExpansion() {

        override fun getIdentifier(): String {
            return this@PlaceholderExpansionWrapper.identifier
        }

        override fun getAuthor(): String {
            return this@PlaceholderExpansionWrapper.author
        }

        override fun getVersion(): String {
            return this@PlaceholderExpansionWrapper.version
        }

        override fun persist(): Boolean {
            return true
        }

        override fun onRequest(player: OfflinePlayer?, params: String): String {
            return handler.apply(player, params)
        }

        override fun onPlaceholderRequest(player: Player?, params: String): String {
            return handler.apply(player, params)
        }

    }

    fun register() {
        expansion.register()
    }

    fun unregister() {
        expansion.unregister()
    }

}