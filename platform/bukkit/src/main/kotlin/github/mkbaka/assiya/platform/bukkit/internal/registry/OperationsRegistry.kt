package github.mkbaka.assiya.platform.bukkit.internal.registry

import github.mkbaka.assiya.common.operation.NumberOperations
import github.mkbaka.assiya.common.operation.Operation
import github.mkbaka.assiya.common.operation.TextOperations


object OperationsRegistry : Registry<Operation<*>>("operation") {

    fun <T : Any> of(key: String): Operation<T> {
        return this[key] as Operation<T>
    }

    init {
        register("add", NumberOperations.Add)
        register("multiply", NumberOperations.Multiply)
        register("max", NumberOperations.Max)
        register("min", NumberOperations.Min)
        register("last", TextOperations.Override)
        register("append", TextOperations.Append)
        register("skip", TextOperations.Skip)
    }

}
