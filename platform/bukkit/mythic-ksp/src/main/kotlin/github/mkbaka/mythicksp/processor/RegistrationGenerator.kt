package github.mkbaka.mythicksp.processor

internal fun renderRegistrations(
    generatedPackage: String,
    hookClass: String,
    mechanics: Map<String, String>,
    conditions: Map<String, String>
): String = buildString {
    val classes = (listOf(hookClass) + mechanics.values + conditions.values).distinct().sorted()
    val simpleNames = classes.groupBy { it.substringAfterLast('.') }
    val references = classes.associateWith { className ->
        val simpleName = className.substringAfterLast('.')
        if (simpleNames.getValue(simpleName).size == 1) simpleName else className
    }
    appendLine("package $generatedPackage")
    appendLine()
    for (className in classes) {
        if (references.getValue(className) != className) {
            appendLine("import $className")
        }
    }
    appendLine()
    appendLine("internal fun ${references.getValue(hookClass)}.registerEntries() {")
    appendRegistrations("registerMechanic", mechanics, references)
    appendRegistrations("registerCondition", conditions, references)
    appendLine("}")
}

private fun StringBuilder.appendRegistrations(
    method: String,
    entries: Map<String, String>,
    references: Map<String, String>
) {
    val byClass = entries.entries.groupBy({ it.value }, { it.key })
    for ((className, names) in byClass.toSortedMap()) {
        val reference = references.getValue(className)
        val aliases = names.sorted().joinToString(", ", transform = ::quote)
        appendLine("    $method($reference::class.java, $aliases)")
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
