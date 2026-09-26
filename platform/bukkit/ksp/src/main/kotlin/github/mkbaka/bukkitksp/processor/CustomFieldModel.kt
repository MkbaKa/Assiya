package github.mkbaka.bukkitksp.processor

import com.google.devtools.ksp.symbol.KSFile

data class CustomFieldModel(
    val objectName: String,
    val propertyName: String,
    val fieldType: String,
    val sourceFile: KSFile?,
)
