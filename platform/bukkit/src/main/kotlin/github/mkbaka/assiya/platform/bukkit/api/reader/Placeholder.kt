package github.mkbaka.assiya.platform.bukkit.api.reader

import github.mkbaka.assiya.common.base.Status

fun interface Placeholder {

    fun resolve(status: Status<*>): Double

}