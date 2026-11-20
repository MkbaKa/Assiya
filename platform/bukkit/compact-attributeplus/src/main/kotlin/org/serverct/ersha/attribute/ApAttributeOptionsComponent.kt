package org.serverct.ersha.attribute

import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent

class ApAttributeOptionsComponent(
    values: MutableMap<String, Any?> = hashMapOf()
) : AttributeComponent, MutableMap<String, Any?> by values {

    override val key: AttributeComponent.Key<ApAttributeOptionsComponent> = Key

    companion object Key : AttributeComponent.Key<ApAttributeOptionsComponent>

}
