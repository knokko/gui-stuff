package procmodel.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.types.*
import procmodel.program.PmProgramBody

class TestDynamicParameterProcessor {

    @Test
    fun testEmptyMapResult() {
        val instructions = listOf(
            PmInstruction.pushValue(PmMap(), 1),
            PmInstruction.invokeBuiltinFunction("outputValue", 1),
            PmInstruction.delete(1),
            PmInstruction.exitProgram(2)
        )
        val processor = PmDynamicParameterProcessor(PmProgramBody(instructions), emptyMap(), emptyMap())
        processor.execute()
        assertEquals(PmMap(), processor.result)
    }

    @Test
    fun testNoResult() {
        val instructions = listOf(PmInstruction.exitProgram(1))
        val processor = PmDynamicParameterProcessor(PmProgramBody(instructions), emptyMap(), emptyMap())
        assertThrows<PmRuntimeError>(processor::execute)
    }

    @Test
    fun testIntResult() {
        val instructions = listOf(
            PmInstruction.pushValue(PmInt(123), 1),
            PmInstruction.invokeBuiltinFunction("outputValue", 1),
            PmInstruction.delete(1),
            PmInstruction.exitProgram(2)
        )
        val processor = PmDynamicParameterProcessor(PmProgramBody(instructions), emptyMap(), emptyMap())
        assertThrows<PmRuntimeError>(processor::execute)
    }

    @Test
    fun testSimpleResult() {
        val instructions = listOf(
            PmInstruction.pushValue(PmMap(), 1),
            PmInstruction.declareVariable("mapping", PmBuiltinTypes.MAP, 1),
            PmInstruction.pushVariable("mapping", 2),
            PmInstruction.pushValue(PmString("angle"), 2),
            PmInstruction.pushValue(PmFloat(25f), 2),
            PmInstruction.writeIndexed(2),
            PmInstruction.pushVariable("mapping", 3),
            PmInstruction.invokeBuiltinFunction("outputValue", 3),
            PmInstruction.delete(3),
            PmInstruction.exitProgram(4)
        )
        val processor = PmDynamicParameterProcessor(PmProgramBody(instructions), emptyMap(), emptyMap())
        processor.execute()

        val expected = PmMap()
        expected.map[PmString("angle")] = PmFloat(25f)
        assertEquals(expected, processor.result)
    }

    @Test
    fun testParameters() {
        val instructions = listOf(
            PmInstruction.pushValue(PmMap(), 1),
            PmInstruction.declareVariable("mapping", PmBuiltinTypes.MAP, 1),
            PmInstruction.pushVariable("mapping", 2),
            PmInstruction.pushValue(PmString("angle"), 2),
            PmInstruction.pushVariable("angle", 2),
            PmInstruction.writeIndexed(2),
            PmInstruction.pushVariable("mapping", 3),
            PmInstruction.invokeBuiltinFunction("outputValue", 3),
            PmInstruction.delete(3),
            PmInstruction.exitProgram(4)
        )

        val parameterTypes = mapOf(Pair("angle", PmFatType(PmBuiltinTypes.FLOAT, null)))
        val parameterValues = mapOf(Pair("angle", PmFloat(25f)))
        val processor = PmDynamicParameterProcessor(PmProgramBody(instructions), parameterValues, parameterTypes)
        processor.execute()

        val expected = PmMap()
        expected.map[PmString("angle")] = PmFloat(25f)
        assertEquals(expected, processor.result)
    }
}
