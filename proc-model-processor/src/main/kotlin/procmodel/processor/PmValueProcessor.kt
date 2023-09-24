package procmodel.processor

import procmodel.lang.functions.PmBuiltinFunctions
import procmodel.lang.types.PmNone
import procmodel.lang.types.PmValue
import procmodel.program.PmProgramBody

class PmValueProcessor(
    body: PmProgramBody,
): PmProcessor(body) {

    var result: PmValue? = null

    fun execute() {
        executeInstructions()

        if (valueStack.isNotEmpty()) throw IllegalStateException("Value stack should be empty")
        if (variables.hasScope()) throw IllegalStateException("All scopes should have been popped")
    }

    override fun invokeBuiltinFunction(name: String) {
        when (name) {
            "outputValue" -> PmBuiltinFunctions.OUTPUT_VALUE.invoke(valueStack) { value -> result = value[0]; PmNone() }
            else -> super.invokeBuiltinFunction(name)
        }
    }
}
