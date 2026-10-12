package github.mkbaka.assiya.platform.bukkit.api.attribute

import java.util.function.Consumer

class SimpleAttribute(
    override val id: String,
    override val components: Map<AttributeComponent.Key<*>, AttributeComponent>
) : Attribute {

    constructor(id: String, builder: Consumer<AttributeComponentBuilder>) : this(
        id, AttributeComponentBuilder().apply(builder::accept).build()
    )

}
