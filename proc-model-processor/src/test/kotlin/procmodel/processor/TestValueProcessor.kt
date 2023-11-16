package procmodel.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.instructions.PmInstruction.Companion.add
import procmodel.lang.instructions.PmInstruction.Companion.declareVariable
import procmodel.lang.instructions.PmInstruction.Companion.delete
import procmodel.lang.instructions.PmInstruction.Companion.divide
import procmodel.lang.instructions.PmInstruction.Companion.duplicate
import procmodel.lang.instructions.PmInstruction.Companion.exitProgram
import procmodel.lang.instructions.PmInstruction.Companion.invokeBuiltinFunction
import procmodel.lang.instructions.PmInstruction.Companion.jump
import procmodel.lang.instructions.PmInstruction.Companion.multiply
import procmodel.lang.instructions.PmInstruction.Companion.popScope
import procmodel.lang.instructions.PmInstruction.Companion.pushScope
import procmodel.lang.instructions.PmInstruction.Companion.pushValue
import procmodel.lang.instructions.PmInstruction.Companion.pushVariable
import procmodel.lang.instructions.PmInstruction.Companion.readIndexed
import procmodel.lang.instructions.PmInstruction.Companion.reassignVariable
import procmodel.lang.instructions.PmInstruction.Companion.setProperty
import procmodel.lang.instructions.PmInstruction.Companion.smallerOrEqual
import procmodel.lang.instructions.PmInstruction.Companion.smallerThan
import procmodel.lang.instructions.PmInstruction.Companion.subtract
import procmodel.lang.instructions.PmInstruction.Companion.swap
import procmodel.lang.instructions.PmInstruction.Companion.writeIndexed
import procmodel.lang.types.*
import procmodel.program.PmProgramBody

class TestValueProcessor {

    @Test
    fun testDeclareAndPushBasicVariable() {
        val instructions = listOf(
            pushValue(PmInt(30), 1),
            declareVariable("hi", PmBuiltinTypes.INT, 1),

            pushVariable("hi", 2),
            invokeBuiltinFunction("outputValue", 2),
            delete(2),

            exitProgram(3)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmInt(30), processor.result)
    }

    @Test
    fun testPushProperty() {
        val instructions = listOf(
            pushValue(PmFloat(1f), 1),
            pushValue(PmFloat(1f), 1),
            pushValue(PmFloat(0f), 1),
            invokeBuiltinFunction("rgb", 1),

            PmInstruction.pushProperty("red", 1),
            invokeBuiltinFunction("outputValue", 1),
            delete(1),
            exitProgram(2)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmFloat(1f), processor.result)
    }

    @Test
    fun testReadList() {
        val instructions = listOf(
            pushValue(PmList(mutableListOf(PmInt(11))), 1),
            declareVariable("testList", PmBuiltinTypes.LIST, 1),

            pushVariable("testList", 2),
            pushValue(PmInt(0), 2),
            readIndexed(2),
            invokeBuiltinFunction("outputValue", 2),
            delete(2),
            exitProgram(3)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmInt(11), processor.result)
    }

    @Test
    fun testUpdateList() {
        val instructions = listOf(
            pushValue(PmList(mutableListOf(PmInt(11))), 1),
            declareVariable("testList", PmBuiltinTypes.LIST, 1),

            pushVariable("testList", 2),
            pushValue(PmInt(0), 2),
            pushValue(PmInt(22), 2),
            writeIndexed(2),

            pushVariable("testList", 3),
            invokeBuiltinFunction("outputValue", 3),
            delete(3),
            exitProgram(4)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmList(mutableListOf(PmInt(22))), processor.result)
    }

    private fun testArithmetic(left: Int, right: Int, result: Int, instruction: PmInstruction) {
        val instructions = listOf(
            pushValue(PmInt(left), 1),
            pushValue(PmInt(right), 1),
            instruction,
            invokeBuiltinFunction("outputValue", 1),
            delete(1),
            exitProgram(2)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmInt(result), processor.result)
    }

    @Test
    fun testDivide() {
        testArithmetic(100, 20, 5, divide(1))
    }

    @Test
    fun testMultiply() {
        testArithmetic(100, 30, 3000, multiply(1))
    }

    @Test
    fun testAdd() {
        testArithmetic(80, 50, 130, add(1))
    }

    @Test
    fun testSubtract() {
        testArithmetic(80, 50, 30, subtract(1))
    }

    private fun testCompare(left: Int, right: Int, result: Boolean, instruction: PmInstruction) {
        val instructions = listOf(
            pushValue(PmInt(left), 1),
            pushValue(PmInt(right), 1),
            instruction,
            invokeBuiltinFunction("outputValue", 1),
            delete(1),
            exitProgram(2)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmBoolean(result), processor.result)
    }

    @Test
    fun testSmallerThan() {
        testCompare(10, 10, false, smallerThan(1))
        testCompare(9, 10, true, smallerThan(1))
        testCompare(10, 9, false, smallerThan(1))
    }

    @Test
    fun testSmallerOrEqual() {
        testCompare(10, 10, true, smallerOrEqual(1))
        testCompare(9, 10, true, smallerOrEqual(1))
        testCompare(10, 9, false, smallerThan(1))
    }

    @Test
    fun testDuplicate() {
        val instructions = listOf(
            pushValue(PmInt(5), 1),
            duplicate(1),
            add(1),
            invokeBuiltinFunction("outputValue", 1),
            delete(1),
            exitProgram(2)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmInt(10), processor.result)
    }

    @Test
    fun testSwap() {
        val instructions = listOf(
            pushValue(PmInt(5), 1),
            pushValue(PmInt(20), 1),
            swap(1),
            subtract(1),
            invokeBuiltinFunction("outputValue", 1),
            delete(1),
            exitProgram(2)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmInt(15), processor.result)
    }

    @Test
    fun testReassignVariable() {
        val instructions = listOf(
            pushValue(PmString("world"), 1),
            declareVariable("hello", PmBuiltinTypes.STRING, 1),

            pushValue(PmString("knokko"), 2),
            reassignVariable("hello", 2),

            pushVariable("hello", 3),
            invokeBuiltinFunction("outputValue", 3),
            delete(3),

            exitProgram(4)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmString("knokko"), processor.result)
    }

    @Test
    fun testSetProperty() {
        var state = 0
        class PmDummy : PmValue() {

            override fun copy() = this

            override fun setProperty(propertyName: String, newValue: PmValue) {
                assertEquals("state", propertyName)
                state = newValue.intValue()
            }
        }

        val instructions = listOf(
            pushValue(PmDummy(), 1),

            pushValue(PmInt(8), 2),
            setProperty("state", 2),
            exitProgram(3)
        )
        PmValueProcessor(PmProgramBody(instructions)).execute()
        assertEquals(8, state)
    }

    @Test
    fun testDoNotJump() {
        val instructions = listOf(
            pushValue(PmBoolean(false), 1),
            pushValue(PmInt(4), 2),
            jump(3),

            pushValue(PmFloat(2f), 4),
            invokeBuiltinFunction("outputValue", 4),
            delete(4),

            exitProgram(5)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmFloat(2f), processor.result)
    }

    @Test
    fun testJump() {
        val instructions = listOf(
            pushValue(PmBoolean(true), 1),
            pushValue(PmInt(5), 2),
            jump(3),

            pushValue(PmFloat(2f), 4),
            invokeBuiltinFunction("outputValue", 4),
            delete(4),
            exitProgram(5),

            pushValue(PmString("jumped"), 6),
            invokeBuiltinFunction("outputValue", 6),
            delete(6),
            exitProgram(7),
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmString("jumped"), processor.result)
    }

    @Test
    fun testScoping() {
        val instructions = listOf(
            pushValue(PmInt(1), 1),
            declareVariable("shadow", PmBuiltinTypes.INT, 1),
            pushScope(2),

            pushValue(PmInt(5), 3),
            declareVariable("shadow", PmBuiltinTypes.INT, 3),

            pushVariable("shadow", 4),
            popScope(5),

            pushVariable("shadow", 6),

            subtract(7),
            invokeBuiltinFunction("outputValue", 7),
            delete(7),
            exitProgram(8)
        )
        val processor = PmValueProcessor(PmProgramBody(instructions))
        processor.execute()
        assertEquals(PmInt(4), processor.result)
    }
}
