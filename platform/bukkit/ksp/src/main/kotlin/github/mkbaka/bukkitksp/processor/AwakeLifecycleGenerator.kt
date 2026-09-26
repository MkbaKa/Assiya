package github.mkbaka.bukkitksp.processor

import github.mkbaka.bukkitksp.annotation.LifeCycle

private val AWAKE_COMPARATOR: Comparator<AwakeModel> =
    compareBy<AwakeModel> { it.priority }
        .thenBy { it.objectName.substringAfterLast('.') }
        .thenBy { it.functionName }

fun renderAwakeLifecycle(generatedPackage: String, pluginInstance: String, models: List<AwakeModel>): String {
    val modelsByLifeCycle = models.groupBy { it.lifeCycle }

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
        appendLine("object $AWAKE_LIFECYCLE_NAME {")

        if (models.isNotEmpty()) {
            appendLine()
            appendLine("    private val logger get() = $pluginInstance.logger")
        }

        for (lifeCycle in LifeCycle.entries) {
            appendLifeCycle(lifeCycle.name.lowercase(), modelsByLifeCycle[lifeCycle].orEmpty(), collidingSimples)
        }

        appendLine()
        appendLine("}")
    }
}

private fun StringBuilder.appendLifeCycle(
    functionName: String,
    models: List<AwakeModel>,
    collidingSimples: Set<String>,
) {
    val entries = models.sortedWith(AWAKE_COMPARATOR)
    appendLine()
    appendLine("    fun $functionName() {")
    for (entry in entries) {
        val callName = resolveCallName(entry.objectName, collidingSimples)
        val callExpr = "$callName.${entry.functionName}()"
        appendLine("        try {")
        appendLine("            $callExpr")
        appendLine("        } catch (e: Throwable) {")
        appendLine("            logger.log(Level.SEVERE, \"Failed to invoke $callExpr\", e)")
        appendLine("        }")
    }
    appendLine("    }")
}
