package procmodel.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmBuiltinTypes
import procmodel.lang.types.PmFloat
import procmodel.lang.types.PmInt
import procmodel.scope.PmVariableScope

class TestParameterHelper {

    @Test
    fun testInitializeNoVariables() {
        val variables = PmVariableScope()
        initializeParameters(variables, "test", emptyMap(), emptyMap())
        assertTrue(variables.hasScope())
    }

    @Test
    fun testInitializeSimpleVariables() {
        val variables = PmVariableScope()
        val parameterTypes = mapOf(
            Pair("count", PmBuiltinTypes.INT),
            Pair("weight", PmBuiltinTypes.FLOAT)
        )
        val parameterValues = mapOf(
            Pair("count", PmInt(5)),
            Pair("weight", PmFloat(1.5f))
        )
        initializeParameters(variables, "test", parameterTypes, parameterValues)
        assertEquals(PmInt(5), variables.getVariable("count"))
        assertEquals(PmFloat(1.5f), variables.getVariable("weight"))
    }

    @Test
    fun testInitializeTooManyVariables() {
        val parameterTypes = mapOf(
            Pair("weight", PmBuiltinTypes.FLOAT)
        )
        val parameterValues = mapOf(
            Pair("count", PmInt(5)),
            Pair("weight", PmFloat(1.5f))
        )
        assertThrows<PmRuntimeError> {
            initializeParameters(PmVariableScope(), "test", parameterTypes, parameterValues)
        }
    }

    @Test
    fun testInitializeNotEnoughVariables() {
        val parameterTypes = mapOf(
            Pair("count", PmBuiltinTypes.INT),
            Pair("weight", PmBuiltinTypes.FLOAT)
        )
        val parameterValues = mapOf(
            Pair("weight", PmFloat(1.5f))
        )
        assertThrows<PmRuntimeError> {
            initializeParameters(PmVariableScope(), "test", parameterTypes, parameterValues)
        }
    }
}
