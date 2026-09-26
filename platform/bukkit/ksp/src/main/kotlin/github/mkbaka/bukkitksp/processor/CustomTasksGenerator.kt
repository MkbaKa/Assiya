package github.mkbaka.bukkitksp.processor

private val CUSTOM_TASK_COMPARATOR: Comparator<CustomTaskModel> =
    compareBy<CustomTaskModel> { it.priority }
        .thenBy { it.objectName.substringAfterLast('.') }
        .thenBy { it.functionName }

fun renderCustomTasks(generatedPackage: String, pluginInstance: String, models: List<CustomTaskModel>): String {
    val byTaskId = models
        .groupBy { it.taskId }
        .toSortedMap()
        .mapValues { (_, entries) -> entries.sortedWith(CUSTOM_TASK_COMPARATOR) }

    val allObjectNames = models.map { it.objectName }.distinct()
    val collidingSimples = findCollidingSimpleNames(allObjectNames)

    val imports = buildList {
        addAll(allObjectNames.filter { it.substringAfterLast('.') !in collidingSimples })
        if (models.isNotEmpty()) {
            add("java.util.logging.Level")
        }
    }.sorted()

    return buildString {
        appendLine("package $generatedPackage")
        appendLine()
        if (imports.isNotEmpty()) {
            for (import in imports) {
                appendLine("import $import")
            }
            appendLine()
        }
        appendLine("object $CUSTOM_TASKS_NAME {")
        appendLine()

        if (models.isEmpty()) {
            appendLine("    fun run(taskId: String) {")
            appendLine("    }")
        } else {
            appendLine("    private val logger get() = $pluginInstance.logger")
            appendLine()
            appendLine("    fun run(taskId: String) {")
            appendLine("        when (taskId) {")

            for ((taskId, entries) in byTaskId) {
                appendLine("            \"$taskId\" -> {")
                for (entry in entries) {
                    val callName = resolveCallName(entry.objectName, collidingSimples)
                    val callExpr = "$callName.${entry.functionName}()"
                    appendLine("                try {")
                    appendLine("                    $callExpr")
                    appendLine("                } catch (e: Throwable) {")
                    appendLine("                    logger.log(Level.SEVERE, \"Failed to invoke $callExpr\", e)")
                    appendLine("                }")
                }
                appendLine("            }")
            }

            appendLine("        }")
            appendLine("    }")
        }

        appendLine()
        appendLine("}")
    }
}
