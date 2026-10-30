package github.mkbaka.assiya.platform.bukkit.api.handler

import github.mkbaka.assiya.common.handler.Processor
import github.mkbaka.assiya.common.handler.ProcessorComponentBuilder
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CallbackComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableAttributeComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CancellableComponent
import github.mkbaka.assiya.platform.bukkit.api.handler.component.CustomDataComponent
import github.mkbaka.assiya.platform.bukkit.internal.config.FormulaManager
import java.util.function.Consumer

/**
 * 属性执行流程
 */
interface BukkitProcessor : Processor {

    /**
     * 基于变量计算公式结果
     *
     * @param [id] 公式名
     * @param [variables] 变量
     * @return [Double]
     */
    fun formula(id: String, variables: Map<String, Any?>): Double {
        return FormulaManager.invoke(id, variables)
    }

    fun formula(id: String, builder: Consumer<Map<String, Any?>>): Double {
        return formula(id, hashMapOf<String, Any?>().apply(builder::accept))
    }

    companion object {

        fun defaults(): ProcessorComponentBuilder {
            return ProcessorComponentBuilder().also { pcb ->
                pcb += CallbackComponent()
                pcb += CancellableAttributeComponent()
                pcb += CancellableComponent()
                pcb += CustomDataComponent()
            }
        }

    }

}
