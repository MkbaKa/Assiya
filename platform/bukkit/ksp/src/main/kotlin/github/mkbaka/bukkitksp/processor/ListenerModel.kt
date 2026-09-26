package github.mkbaka.bukkitksp.processor

import com.google.devtools.ksp.symbol.KSFile

data class ListenerModel(
    val objectName: String,
    val objectSimpleName: String,
    val sourceFile: KSFile?,
    val handlers: List<EventHandlerModel>,
)
