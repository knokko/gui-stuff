package procmodel.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.types.PmBuiltinTypes
import procmodel.lang.types.PmInt
import procmodel.model.PmDynamicMatrix
import procmodel.program.PmDynamicMatrixConstructor

class TestMatrixProcessor {

    @Test
    fun testSimpleResult() {
        val instructions = listOf(
            PmInstruction.pushValue(PmInt(123), 1),
            PmInstruction.invokeBuiltinFunction("outputValue", 1),
            PmInstruction.delete(1),
            PmInstruction.exitProgram(2)
        )
        val dynamicMatrix = PmDynamicMatrix(
            PmDynamicMatrixConstructor(instructions), null, emptyMap(), emptyMap()
        )
        val processor = PmMatrixProcessor(dynamicMatrix, emptyMap())
        processor.execute()
        assertEquals(PmInt(123), processor.result)
    }

    @Test
    fun testTransferVariablesAndDynamicParameters() {
        val instructions = listOf(
            PmInstruction.pushVariable("trans", 1),
            PmInstruction.pushVariable("param", 1),
            PmInstruction.add(1),
            PmInstruction.invokeBuiltinFunction("outputValue", 2),
            PmInstruction.delete(2),
            PmInstruction.exitProgram(3)
        )
        val dynamicMatrix = PmDynamicMatrix(
            PmDynamicMatrixConstructor(instructions),
            null, mapOf(Pair("param", PmBuiltinTypes.INT)),
            mapOf(Pair("trans", Pair(PmBuiltinTypes.INT, PmInt(25))))
        )
        val processor = PmMatrixProcessor(dynamicMatrix, mapOf(Pair("param", PmInt(10))))
        processor.execute()
        assertEquals(PmInt(35), processor.result)
    }
}
