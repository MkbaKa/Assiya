package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.BukkitProcessor
import groovy.transform.CompileStatic

@CompileStatic
void onTimer(Attribute attribute, BukkitProcessor processor) {
    println('onTimer!')
}
