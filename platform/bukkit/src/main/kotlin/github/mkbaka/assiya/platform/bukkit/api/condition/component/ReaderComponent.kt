package github.mkbaka.assiya.platform.bukkit.api.condition.component

import github.mkbaka.assiya.platform.bukkit.api.condition.ConditionComponent
import github.mkbaka.assiya.platform.bukkit.api.reader.Reader
import github.mkbaka.assiya.platform.bukkit.internal.registry.ReaderRegistry

class ReaderComponent(
    val readerId: String,
    val names: List<String>
) : ConditionComponent {

    val reader: Reader
        get() = ReaderRegistry[readerId] ?: error("Reader not found: $readerId")

    override val key: ConditionComponent.Key<ReaderComponent> = Key

    companion object Key : ConditionComponent.Key<ReaderComponent>

}
