package github.mkbaka.assiya.platform.bukkit.internal.config

import github.mkbaka.assiya.platform.bukkit.api.config.ConfigNode
import github.mkbaka.assiya.platform.bukkit.internal.formula.Formula
import github.mkbaka.assiya.platform.bukkit.internal.formula.FormulaCompiler
import github.mkbaka.bukkitksp.annotation.CustomTask

object FormulaManager : AbstractConfigManager<String, Formula>(
    "formula"
//    ::readExpression,
//    FormulaCompiler::compile,
) {

    override fun defaultResources(): List<String> {
        return listOf(
            "formula/example.yml",
            "formula/attributes.yml",
        )
    }

    override fun readConfig(node: ConfigNode, key: String): String? {
        return node.getString(key)
    }

    override fun convert(id: String, value: String): Formula {
        return FormulaCompiler.compile(id, value)
    }

    /**
     * TODO:
     * 传递的参数在 js 中构造, 那么像随机值/papi变量等占位符都可以在公式配置里使用
     * 物理伤害 js:
     * const map = new HashMap()
     * map["total"] = readAttr("total")
     * map["random"] = Math.random()
     * map["level"] = PAPI.parse("%player_level%")
     * FormulaManager.invoke("physical", )
     */
    fun invoke(id: String, bindings: Map<String, Any?>): Double {
        val formula = get(id)
            ?: throw IllegalArgumentException("Formula $id does not exist or failed to compile")
        return formula.invoke(bindings::get)
    }

    @CustomTask(taskId = "reload", priority = 15)
    override fun reload() {
        super.reload()
    }

}
