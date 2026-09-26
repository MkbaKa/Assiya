package github.mkbaka.assiya.platform.bukkit.api.attribute.trigger

interface Trigger {

    val id: String

    val function: String

    val indexId: String
        get() = id

    data class Base(
        override val id: String,
        override val function: String,
        override val indexId: String = id
    ) : Trigger

}
