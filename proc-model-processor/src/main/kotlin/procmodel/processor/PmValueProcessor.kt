package procmodel.processor

import procmodel.lang.functions.PmBuiltinFunction
import procmodel.lang.types.PmBuiltinTypes
import procmodel.lang.types.PmNone
import procmodel.lang.types.PmValue
import procmodel.program.PmProgramBody

open class PmValueProcessor(
    body: PmProgramBody,
): PmProcessor(body) {

    var result: PmValue? = null

    init {
        addBuiltinFunction("outputValue", PmBuiltinFunction(listOf(PmBuiltinTypes.ANY), PmBuiltinTypes.VOID) {
            value -> result = value[0]; PmNone()
        })
    }

    open fun execute() {
        executeInstructions()

        if (valueStack.isNotEmpty()) throw IllegalStateException("Value stack should be empty")
        if (variables.hasScope()) throw IllegalStateException("All scopes should have been popped")
    }
}
