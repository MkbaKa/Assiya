package github.mkbaka.bukkitksp.processor

import com.google.devtools.ksp.symbol.KSFile

data class CustomTaskModel(
    val objectName: String,
    val functionName: String,
    val taskId: String,
    val priority: Int,
    val sourceFile: KSFile?,
)
