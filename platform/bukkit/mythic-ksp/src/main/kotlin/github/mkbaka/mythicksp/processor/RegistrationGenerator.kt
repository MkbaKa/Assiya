package github.mkbaka.mythicksp.processor

internal data class ConditionVariant(val className: String, val requiresClass: String)

internal fun renderRegistrations(
    generatedPackage: String,
    hookClass: String,
    mechanics: Map<String, String>,
    conditions: Map<String, String>,
    variants: Map<String, ConditionVariant> = emptyMap()
): String = buildString {
    val classes = (listOf(hookClass) + mechanics.values + conditions.values).distinct().sorted()
    val simpleNames = classes.groupBy { it.substringAfterLast('.') }
    val references = classes.associateWith { className ->
        val simpleName = className.substringAfterLast('.')
        if (simpleNames.getValue(simpleName).size == 1) simpleName else className
    }
    appendLine("package $generatedPackage")
    appendLine()
    if (variants.isNotEmpty()) {
        appendLine("import github.mkbaka.assiya.platform.bukkit.internal.util.hasClass")
    }
    for (className in classes) {
        if (references.getValue(className) != className) {
            appendLine("import $className")
        }
    }
    appendLine()
    appendLine("internal fun ${references.getValue(hookClass)}.registerEntries() {")
    appendRegistrations("registerMechanic", mechanics, references)
    appendRegistrations("registerCondition", conditions, references, variants)
    appendLine("}")
}

private fun StringBuilder.appendRegistrations(
    method: String,
    entries: Map<String, String>,
    references: Map<String, String>,
    variants: Map<String, ConditionVariant> = emptyMap()
) {
    val byClass = entries.entries.groupBy({ it.value }, { it.key })
    for ((className, names) in byClass.toSortedMap()) {
        val reference = references.getValue(className)
        val aliases = names.sorted().joinToString(", ", transform = ::quote)
        val variant = variants[className]
        if (variant == null) {
            appendLine("    $method($reference::class.java, $aliases)")
        } else {
            appendLine("    $method(")
            appendLine("        if (hasClass(${quote(variant.requiresClass)})) {")
            appendLine("            Class.forName(${quote(variant.className)}, false, $reference::class.java.classLoader)")
            appendLine("                .asSubclass($reference::class.java)")
            appendLine("        } else $reference::class.java,")
            appendLine("        $aliases")
            appendLine("    )")
        }
    }
}

private fun quote(value: String): String = buildString {
    append('"')
    for (char in value) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '$' -> append("\\$")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}
