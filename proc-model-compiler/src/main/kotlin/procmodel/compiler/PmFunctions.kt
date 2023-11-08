package procmodel.compiler

import procmodel.exceptions.PmCompileError
import procmodel.lang.types.PmType

internal class PmFunctions {

    private val scopes = mutableListOf<MutableMap<PmFunctionSignature, PmFunction>>()

    fun pushScope() {
        scopes.add(mutableMapOf())
    }

    fun popScope() {
        scopes.removeLast()
    }

    fun defineUserFunction(signature: PmFunctionSignature, function: PmFunction) {
        val lastScope = scopes.last()
        if (lastScope.containsKey(signature)) {
            throw PmCompileError("Duplicate function $signature")
        }

        lastScope[signature] = function
    }

    fun getUserFunction(signature: PmFunctionSignature): PmFunction {
        for (scope in scopes.reversed()) {
            val maybeFunction = scope[signature]
            if (maybeFunction != null) return maybeFunction
        }
        throw PmCompileError("Unknown function $signature")
    }
}

internal class PmFunction(
    val startInstruction: Int,
    val returnType: PmType
)

internal class PmFunctionSignature(
    val name: String,
    val numParameters: Int
) {
    override fun toString() = "$name($numParameters parameters)"

    override fun equals(other: Any?) = other is PmFunctionSignature
            && other.name == name && other.numParameters == numParameters

    override fun hashCode() = name.hashCode() + 123 * numParameters
}
