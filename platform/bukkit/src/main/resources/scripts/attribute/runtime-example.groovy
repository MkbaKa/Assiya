package scripts.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.Attribute
import github.mkbaka.assiya.platform.bukkit.api.handler.Processor
import groovy.transform.CompileStatic

@CompileStatic
void onTimer(Attribute attribute, Processor processor) {
    println('onTimer!')
}
