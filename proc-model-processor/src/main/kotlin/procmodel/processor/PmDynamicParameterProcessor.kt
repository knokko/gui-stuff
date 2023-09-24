package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmMap
import procmodel.lang.types.PmString
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue
import procmodel.program.PmProgramBody

internal class PmDynamicParameterProcessor(
    body: PmProgramBody,
    private val dynamicParentParameterValues: Map<String, PmValue>,
    private val dynamicParentParameterTypes: Map<String, PmType>
): PmProcessor(body) {

    val result = mutableMapOf<String, PmValue>()

    fun execute() {
        variables.pushScope()
        for ((key, value) in dynamicParentParameterValues) {
            variables.defineVariable(dynamicParentParameterTypes[key]!!, key, value.copy())
        }
        executeInstructions()

        val resultMap = valueStack.removeLast()
        if (resultMap is PmMap) {
            for ((key, value) in resultMap.map) {
                if (key !is PmString) throw PmRuntimeError("All dynamic parameter map keys must be strings, but got $key")
                result[key.value] = value
            }
        } else {
            throw PmRuntimeError("Expected dynamic parameter map, but got $resultMap")
        }

        if (valueStack.isNotEmpty()) throw IllegalStateException("Value stack should be empty")
        variables.popScope()
        if (variables.hasScope()) throw IllegalStateException("All scopes should have been popped")
    }
}
