package procmodel.scope

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.*
import kotlin.random.Random

class TestVariableScope {

    @Test
    fun testHappyFlow() {
        val scope = PmVariableScope()
        assertFalse(scope.hasScope())

        scope.pushScope()
        assertTrue(scope.hasScope())
        assertNull(scope.getVariable("hello"))

        scope.defineVariable(PmBuiltinTypes.STRING, "hello", PmString("world"))
        scope.defineVariable(PmBuiltinTypes.ANY, "wildcard", PmInt(123))

        assertEquals(PmString("world"), scope.getVariable("hello"))
        assertEquals(PmInt(123), scope.getVariable("wildcard"))
        assertNull(scope.getVariable("nope"))

        scope.reassignVariable("hello", PmString("hi"))
        assertEquals(PmString("hi"), scope.getVariable("hello"))

        scope.pushScope()
        assertEquals(PmInt(123), scope.getVariable("wildcard"))
        assertEquals(PmString("hi"), scope.getVariable("hello"))

        scope.reassignVariable("wildcard", PmFloat(1f))
        assertEquals(PmFloat(1f), scope.getVariable("wildcard"))

        scope.defineVariable(PmBuiltinTypes.INT, "bye", PmInt(12))

        scope.popScope()
        assertTrue(scope.hasScope())
        assertNull(scope.getVariable("bye"))
        assertEquals(PmString("hi"), scope.getVariable("hello"))

        scope.popScope()
        assertFalse(scope.hasScope())
    }

    @Test
    fun testShadowing() {
        val scope = PmVariableScope()
        scope.pushScope()

        scope.defineVariable(PmBuiltinTypes.INT, "shadow", PmInt(20))

        scope.pushScope()

        scope.defineVariable(PmBuiltinTypes.STRING, "shadow", PmString("cold"))
        assertEquals(PmString("cold"), scope.getVariable("shadow"))

        scope.popScope()
        assertEquals(PmInt(20), scope.getVariable("shadow"))
    }

    @Test
    fun testDuplicateVariableError() {
        val scope = PmVariableScope()
        scope.pushScope()

        scope.defineVariable(PmBuiltinTypes.INT, "dup", PmInt(100))
        assertThrows<PmRuntimeError> { scope.defineVariable(PmBuiltinTypes.INT, "dup", PmInt(100)) }
        assertThrows<PmRuntimeError> { scope.defineVariable(PmBuiltinTypes.INT, "dup", PmInt(80)) }
        assertThrows<PmRuntimeError> { scope.defineVariable(PmBuiltinTypes.STRING, "dup", PmString("diff")) }
    }

    @Test
    fun testTypeMismatchError() {
        val scope = PmVariableScope()
        scope.pushScope()

        assertThrows<PmRuntimeError> { scope.defineVariable(PmBuiltinTypes.RANDOM, "rng", PmString("rng")) }
        scope.defineVariable(PmBuiltinTypes.RANDOM, "rng", PmRandom(Random.Default))
        assertThrows<PmRuntimeError> { scope.reassignVariable("rng", PmString("again")) }
    }
}
