package procmodel.processor

import procmodel.lang.types.PmValue
import procmodel.model.PmDynamicMatrix

class PmMatrixProcessor(
    matrix: PmDynamicMatrix,
    private val dynamicParameterValues: Map<String, PmValue>,
): PmValueProcessor(matrix.construction) {

    private val dynamicParameterTypes = matrix.dynamicParameterTypes
    private val transferredVariables = matrix.transferredVariables

    override fun executeInstructions() {
        initializeParameters(
            variables, "dynamic",
            dynamicParameterTypes.mapValues { it.value.type },
            dynamicParameterValues
        )
        for ((name, typedValue) in transferredVariables) {
            val (type, value) = typedValue
            variables.defineVariable(type, name, value)
        }
        super.executeInstructions()
        variables.popScope()
    }
}
