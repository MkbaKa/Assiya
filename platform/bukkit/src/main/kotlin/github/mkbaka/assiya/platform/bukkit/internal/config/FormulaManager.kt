package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.internal.config.loader.ConfigLoader
import github.mkbaka.assiya.platform.bukkit.internal.formula.Formula
import github.mkbaka.assiya.platform.bukkit.internal.formula.FormulaCompiler
import github.mkbaka.bukkitksp.annotation.CustomTask

object FormulaManager {

    private val formulas = hashMapOf<String, Formula>()

    val size: Int get() = formulas.size

    fun invoke(id: String, bindings: Map<String, Any?>): Double {
        val formula = formulas[id]
            ?: throw IllegalArgumentException("Formula $id does not exist or failed to compile")
        return formula.invoke(bindings::get)
    }

    @CustomTask(taskId = "reload", priority = 15)
    fun reload() {
        formulas.clear()
        ConfigLoader.load("formula", listOf("formula/example.yml", "formula/attributes.yml")) { id, root ->
            val expression = root.getString(id) ?: error("Formula $id must have an expression")
            formulas[id] = FormulaCompiler.compile(id, expression)
        }
    }
}
