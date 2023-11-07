package procmodel.scope

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue

class PmVariableScope {

    private val scopes = mutableListOf<MutableMap<String, Pair<PmType, PmValue>>>()

    fun hasScope() = scopes.isNotEmpty()

    fun pushScope() {
        scopes.add(mutableMapOf())
    }

    fun popScope() {
        scopes.removeLast()
    }

    fun defineVariable(type: PmType, name: String, initialValue: PmValue) {
        val lastScope = scopes.last()
        if (lastScope.containsKey(name)) throw PmRuntimeError("Duplicate variable $name")
        if (!type.acceptValue(initialValue)) throw PmRuntimeError("Type $type doesn't accept $initialValue")
        lastScope[name] = Pair(type, initialValue)
    }

    fun reassignVariable(name: String, newValue: PmValue) {
        for (scope in scopes.reversed()) {
            val maybeVariable = scope[name]
            if (maybeVariable != null) {
                if (!maybeVariable.first.acceptValue(newValue)) {
                    throw PmRuntimeError("$name has type ${maybeVariable.first}, which doesn't accept $newValue")
                }
                scope[name] = Pair(maybeVariable.first, newValue)
                return
            }
        }
        throw PmRuntimeError("Unknown variable $name")
    }

    fun getVariable(name: String): PmValue? {
        for (scope in scopes.reversed()) {
            val maybeVariable = scope[name]
            if (maybeVariable != null) return maybeVariable.second
        }
        return null
    }

    override fun toString() = "PmVariableScope(depth=${scopes.size}, top=${scopes.lastOrNull()})"
}
