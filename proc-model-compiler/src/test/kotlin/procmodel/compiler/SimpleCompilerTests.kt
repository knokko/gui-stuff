package procmodel.compiler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import procmodel.importer.PmImporter
import procmodel.lang.types.*
import procmodel.processor.PmValueProcessor

class SimpleCompilerTests {

    private fun valueTest(sourceCode: String, expected: PmValue) {
        val program = PmCompiler.compile(sourceCode, PmImporter.dummy(), emptyMap(), emptyList())
        val processor = PmValueProcessor(program.body)
        processor.execute()
        assertEquals(expected, processor.result)
    }

    @Test
    fun testOutputValue() {
        valueTest("outputValue(82);", PmInt(82))
    }

    @Test
    fun testAdd() {
        valueTest("outputValue(1 + 2);", PmInt(3))
    }

    @Test
    fun testSubtract() {
        valueTest("outputValue(23 - 8);", PmInt(15))
    }

    @Test
    fun testMultiply() {
        valueTest("outputValue(4.0 * 5.0);", PmFloat(20f))
    }

    @Test
    fun testDivide() {
        valueTest("outputValue(5.0 / 4.0);", PmFloat(1.25f))
        valueTest("outputValue(20 / 3);", PmInt(6))
    }

    @Test
    fun testArithmeticOrdering() {
        valueTest("outputValue(1 + 3 * 2 / (7 - 2 * 2));", PmInt(3))
    }

    @Test
    fun testForLoop1() {
        valueTest("""
            int result = 5;
            for (3 < adder <= 5) {
                result = result + adder;
            }
            outputValue(result);
        """, PmInt(14))
    }

    @Test
    fun testForLoop2() {
        valueTest("""
            int result = 5;
            for (3 <= adder < 5) {
                result = result + adder;
            }
            outputValue(result);
        """, PmInt(12))
    }

    @Test
    fun testLists() {
        valueTest("""
            List test1234 = [1, 2, 3];
            add(test1234, 4);
            test1234 = test1234 + [5, 6] * 2;
            test1234[6] = 7;
            outputValue(test1234);
        """, PmList(mutableListOf(
            PmInt(1), PmInt(2), PmInt(3), PmInt(4),
            PmInt(5), PmInt(6), PmInt(7), PmInt(6)
        )))
    }

    @Test
    fun testSets() {
        valueTest("""
            Set test1234 = {3, 1, 2, 2};
            add(test1234, 4);
            add(test1234, 3);
            outputValue(test1234 + {6, 5, 5});
        """, PmSet(mutableSetOf(
            PmInt(1), PmInt(2), PmInt(3), PmInt(4), PmInt(5), PmInt(6)
        )))
    }

    @Test
    fun testMaps() {
        valueTest("""
            Map habitats;
            habitats["mantid"] = "mines";
            habitats[666] = "hell";
            habitats["bat"] = 123;
            
            outputValue(habitats[666]);
        """, PmString("hell"))
    }

    @Test
    fun testGetProperty() {
        valueTest("""
            color orange = rgb(1.0, 1.0, 0.0);
            float red = orange.red;
            outputValue(red);
        """, PmFloat(1f))
    }

    @Test
    fun testUserFunctions() {
        valueTest("""
            int stupidMultiply(int left, float right) {
                left * int(right)
            }
            outputValue(stupidMultiply(3, 6.0));
        """, PmInt(18))
    }

    @Test
    fun testNestedFunctions() {
        valueTest("""
            float subtractFloats(float left, float right) {
                left - right
            }
            float multiplyFloats(float left, float right) {
                left * right
            }
            float square(float factor) {
                multiplyFloats(factor, factor)
            }
            outputValue(subtractFloats(square(5.0), square(4.0)));
        """, PmFloat(9f))
    }
}
