package procmodel.compiler

import procmodel.exceptions.PmCompileError
import procmodel.lang.types.PmType

internal class PmTypes {

    private val scopes = mutableListOf<MutableMap<String, PmType>>()

    fun pushScope() {
        scopes.add(mutableMapOf())
    }

    fun popScope() {
        scopes.removeLast()
    }

    fun defineType(name: String, type: PmType) {
        val lastScope = scopes.last()
        if (lastScope.containsKey(name)) {
            throw PmCompileError("Duplicate type $name")
        }

        lastScope[name] = type
    }

    fun getType(name: String): PmType {
        for (scope in scopes.reversed()) {
            val maybeType = scope[name]
            if (maybeType != null) return maybeType
        }
        throw PmCompileError("Unknown type $name")
    }
}
