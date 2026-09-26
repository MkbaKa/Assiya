package github.mkbaka.bukkitksp.processor

private val CUSTOM_FIELD_COMPARATOR: Comparator<CustomFieldModel> =
    compareBy<CustomFieldModel> { it.objectName.substringAfterLast('.') }
        .thenBy { it.objectName }
        .thenBy { it.propertyName }

fun renderCustomFields(generatedPackage: String, models: List<CustomFieldModel>): String {
    val byFieldType = models
        .groupBy { it.fieldType }
        .toSortedMap()
        .mapValues { (_, entries) -> entries.sortedWith(CUSTOM_FIELD_COMPARATOR) }

    val allObjectNames = models.map { it.objectName }.distinct()
    val collidingSimples = findCollidingSimpleNames(allObjectNames)

    val imports = allObjectNames
        .filter { it.substringAfterLast('.') !in collidingSimples }
        .sorted()

    return buildString {
        appendLine("package $generatedPackage")
        appendLine()
        if (imports.isNotEmpty()) {
            for (import in imports) {
                appendLine("import $import")
            }
            appendLine()
        }
        appendLine("object $CUSTOM_FIELDS_NAME {")
        appendLine()

        if (models.isEmpty()) {
            appendLine("    private val suppliers: Map<String, List<() -> Any?>> = emptyMap()")
        } else {
            appendLine("    private val suppliers: Map<String, List<() -> Any?>> = mapOf(")
            for ((fieldType, entries) in byFieldType) {
                appendLine("        \"$fieldType\" to listOf(")
                for (entry in entries) {
                    val callName = resolveCallName(entry.objectName, collidingSimples)
                    appendLine("            { $callName.${entry.propertyName} },")
                }
                appendLine("        ),")
            }
            appendLine("    )")
        }

        appendLine()
        appendLine("    fun <T> get(fieldType: String, type: Class<out T>): List<T> {")
        appendLine("        val results = mutableListOf<T>()")
        appendLine("        for (supplier in suppliers[fieldType].orEmpty()) {")
        appendLine("            val value = supplier() ?: continue")
        appendLine("            if (type.isInstance(value)) {")
        appendLine("                results.add(type.cast(value))")
        appendLine("            }")
        appendLine("        }")
        appendLine("        return results")
        appendLine("    }")
        appendLine()
        appendLine("    inline fun <reified T> get(fieldType: String): List<T> =")
        appendLine("        get(fieldType, T::class.java)")
        appendLine()
        appendLine("}")
    }
}
