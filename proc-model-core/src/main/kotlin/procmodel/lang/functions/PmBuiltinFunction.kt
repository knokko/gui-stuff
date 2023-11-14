package procmodel.lang.functions

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue

class PmBuiltinFunction(
    val parameterTypes: List<PmType>,
    val returnType: PmType,
    val implementation: (List<PmValue>) -> PmValue
) {

    fun invoke(valueStack: MutableList<PmValue>) {
        if (valueStack.size < parameterTypes.size) {
            throw PmRuntimeError("Value stack (${valueStack.size} elements) is too small to call this function (${parameterTypes.size} parameters)")
        }
        val parameterValues = parameterTypes.indices.map { valueStack.removeLast() }.reversed()
        for ((index, type) in parameterTypes.withIndex()) {
            if (!type.acceptValue(parameterValues[index])) {
                throw PmRuntimeError("Parameter type $type doesn't accept parameter $index (${parameterValues[index]})")
            }
        }

        val result = implementation(parameterValues)
        if (!returnType.acceptValue(result)) throw PmRuntimeError("Return type ($returnType) doesn't accept result $result")
        valueStack.add(result)
    }
}
