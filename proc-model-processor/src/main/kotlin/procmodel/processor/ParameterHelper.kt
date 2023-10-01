package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue
import procmodel.scope.PmVariableScope

internal fun initializeParameters(
    variables: PmVariableScope, parameterKind: String,
    types: Map<String, PmType>, values: Map<String, PmValue>
) {
    variables.pushScope()

    for ((parameterName, parameterValue) in values) {
        val parameterType = types[parameterName]
            ?: throw PmRuntimeError("Unexpected $parameterKind parameter value for $parameterName")
        variables.defineVariable(parameterType, parameterName, parameterValue)
    }
    for (parameterName in types.keys) {
        if (!values.containsKey(parameterName)) {
            throw PmRuntimeError("Missing $parameterKind parameter $parameterName")
        }
    }
}
