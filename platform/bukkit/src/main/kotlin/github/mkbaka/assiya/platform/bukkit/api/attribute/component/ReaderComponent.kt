package github.mkbaka.assiya.platform.bukkit.api.attribute.component

import github.mkbaka.assiya.common.base.Status
import github.mkbaka.assiya.platform.bukkit.api.attribute.AttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry

class ReaderComponent(
    val readerId: String,
    val names: List<String>
) : AttributeComponent {

    val reader: Reader
        get() = ReaderRegistry[readerId] ?: error("Reader not found: $readerId")

    override val key: AttributeComponent.Key<ReaderComponent> = Key

    companion object Key : AttributeComponent.Key<ReaderComponent>

}
