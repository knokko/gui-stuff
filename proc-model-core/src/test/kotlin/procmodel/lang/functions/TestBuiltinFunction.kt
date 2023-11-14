package procmodel.lang.functions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.*

class TestBuiltinFunction {

    @Test
    fun testInvokeWithoutParameters() {
        val expectedValueStack = mutableListOf<PmValue>(PmBoolean(true))
        val valueStack = mutableListOf<PmValue>(PmBoolean(true))

        val produceIntGood = PmBuiltinFunction(emptyList(), PmBuiltinTypes.INT) { parameters ->
            assertTrue(parameters.isEmpty())
            PmInt(5)
        }
        val produceIntBad1 = PmBuiltinFunction(emptyList(), PmBuiltinTypes.INT) { _ -> PmBoolean(false) }
        val produceIntBad2 = PmBuiltinFunction(emptyList(), PmBuiltinTypes.INT) { _ -> PmNone() }

        produceIntGood.invoke(valueStack)
        expectedValueStack.add(PmInt(5))
        assertEquals(expectedValueStack, valueStack)
        assertThrows<PmRuntimeError> { produceIntBad1.invoke(valueStack) }
        assertThrows<PmRuntimeError> { produceIntBad2.invoke(valueStack) }

        assertEquals(expectedValueStack, valueStack)

        val produceNothingGood = PmBuiltinFunction(emptyList(), PmBuiltinTypes.VOID) { parameters ->
            assertTrue(parameters.isEmpty())
            PmNone()
        }
        val produceNothingBad = PmBuiltinFunction(emptyList(), PmBuiltinTypes.VOID) { _ -> PmInt(5) }

        produceNothingGood.invoke(valueStack)
        expectedValueStack.add(PmNone())
        assertEquals(expectedValueStack, valueStack)
        assertThrows<PmRuntimeError> { produceNothingBad.invoke(valueStack) }

        assertEquals(expectedValueStack, valueStack)
    }

    @Test
    fun testInvokeWithTwoParameters() {
        val valueStack = mutableListOf<PmValue>(PmInt(10), PmInt(8))

        val produceIntGood = PmBuiltinFunction(listOf(PmBuiltinTypes.INT, PmBuiltinTypes.INT), PmBuiltinTypes.INT) { parameters ->
            assertEquals(2, parameters.size)
            parameters[0] - parameters[1]
        }
        val produceIntBad1 = PmBuiltinFunction(listOf(PmBuiltinTypes.INT, PmBuiltinTypes.INT), PmBuiltinTypes.INT) { _ -> PmNone() }
        val produceIntBad2 = PmBuiltinFunction(listOf(PmBuiltinTypes.INT, PmBuiltinTypes.INT), PmBuiltinTypes.INT) { _ -> PmFloat(1f)}
        val produceIntFail = PmBuiltinFunction(listOf(PmBuiltinTypes.INT, PmBuiltinTypes.INT), PmBuiltinTypes.INT) { _ -> fail() }

        produceIntGood.invoke(valueStack)
        assertEquals(listOf(PmInt(2)), valueStack)

        valueStack.add(PmInt(0))
        assertThrows<PmRuntimeError> { produceIntBad1.invoke(valueStack)  }
        valueStack.clear()
        valueStack.addAll(listOf(PmInt(1), PmInt(2)))
        assertThrows<PmRuntimeError> { produceIntBad2.invoke(valueStack)  }

        valueStack.clear()
        valueStack.add(PmString("hello"))
        valueStack.add(PmInt(1))

        assertThrows<PmRuntimeError> { produceIntFail.invoke(valueStack) }

        valueStack.clear()
        valueStack.add(PmInt(5))
        assertThrows<PmRuntimeError> { produceIntFail.invoke(valueStack) }

        var wasCalled = false
        val produceNothingGood = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.STRING), PmBuiltinTypes.VOID){ parameters ->
            assertEquals(PmFloat(3f), parameters[0])
            assertEquals(PmString("it's me"), parameters[1])
            wasCalled = true
            PmNone()
        }
        val produceNothingBad = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.STRING), PmBuiltinTypes.VOID){ _ -> PmFloat(-5f) }
        valueStack.clear()
        valueStack.add(PmFloat(3f))
        valueStack.add(PmString("it's me"))

        produceNothingGood.invoke(valueStack)
        assertTrue(wasCalled)
        assertEquals(listOf(PmNone()), valueStack)

        valueStack.clear()
        valueStack.add(PmFloat(3f))
        valueStack.add(PmString("it's me"))
        assertThrows<PmRuntimeError> { produceNothingBad.invoke(valueStack) }
    }
}
