package github.mkbaka.bukkitksp.processor

import com.google.devtools.ksp.symbol.KSFile
import github.mkbaka.bukkitksp.annotation.LifeCycle

data class AwakeModel(
    val objectName: String,
    val functionName: String,
    val lifeCycle: LifeCycle,
    val priority: Int,
    val sourceFile: KSFile?,
)
