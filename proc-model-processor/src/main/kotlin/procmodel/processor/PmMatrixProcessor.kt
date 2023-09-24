package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue
import procmodel.model.PmDynamicMatrix

class PmMatrixProcessor<M>(
    matrix: PmDynamicMatrix,
    private val dynamicParameterTypes: Map<String, PmType>,
    private val dynamicParameterValues: Map<String, PmValue>,
    private val castResult: (PmValue) -> M
): PmProcessor(matrix.construction) {

    private val transferredVariables = matrix.transferredVariables

    @Throws(PmRuntimeError::class)
    fun execute(): M {
        variables.pushScope()
        for ((name, variable) in transferredVariables) {
            val (type, value) = variable
            variables.defineVariable(type, name, value)
        }
        for ((parameterName, parameterValue) in dynamicParameterValues) {
            val parameterType = dynamicParameterTypes[parameterName]
                ?: throw PmRuntimeError("Unexpected dynamic parameter value for $parameterName")
            variables.defineVariable(parameterType, parameterName, parameterValue)
        }
        for (parameterName in dynamicParameterTypes.keys) {
            if (!dynamicParameterValues.containsKey(parameterName)) {
                throw PmRuntimeError("Missing dynamic parameter $parameterName")
            }
        }
        executeInstructions()
        variables.popScope()

        if (variables.hasScope()) throw PmRuntimeError("Variable scopes aren't empty")

        if (valueStack.size != 1) throw PmRuntimeError("Size of valueStack ($valueStack) is not 1")
        return castResult(valueStack.removeLast())
    }
}
