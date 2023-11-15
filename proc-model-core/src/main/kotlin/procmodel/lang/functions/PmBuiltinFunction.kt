package procmodel.lang.functions

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue

class PmBuiltinFunction(
    val parameterTypes: List<PmType>,
    private val returnType: PmType,
    val implementation: (Array<PmValue>) -> PmValue
) {

    fun invoke(valueStack: MutableList<PmValue>) {
        if (valueStack.size < parameterTypes.size) {
            throw PmRuntimeError("Value stack (${valueStack.size} elements) is too small to call this function (${parameterTypes.size} parameters)")
        }

        val parameterValues = Array(parameterTypes.size) { valueStack.removeLast() }
        parameterValues.reverse()

        for ((index, value) in parameterValues.withIndex()) {
            if (!parameterTypes[index].acceptValue(value)) {
                throw PmRuntimeError("Type ${parameterTypes[index]} doesn't accept $value")
            }
        }

        val result = implementation(parameterValues)
        if (!returnType.acceptValue(result)) throw PmRuntimeError("Return type ($returnType) doesn't accept result $result")
        valueStack.add(result)
    }
}
