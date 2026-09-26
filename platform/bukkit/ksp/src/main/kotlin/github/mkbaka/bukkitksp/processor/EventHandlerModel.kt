package github.mkbaka.bukkitksp.processor

data class EventHandlerModel(
    val functionName: String,
    val eventTypeName: String,
    val priority: String,
    val ignoreCancelled: Boolean,
)
