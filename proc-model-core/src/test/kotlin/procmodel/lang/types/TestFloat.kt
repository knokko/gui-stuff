package procmodel.lang.types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestFloat {

    @Test
    fun testFloatValue() {
        assertEquals(3f, PmFloat(3f).floatValue(), 0f)
    }

    @Test
    fun testEquals() {
        assertEquals(PmFloat(123f), PmFloat(123f))
        assertNotEquals(PmFloat(123f), PmFloat(124f))
    }

    @Test
    fun testArithmetic() {
        assertEquals(10f, (PmFloat(2f) * (PmFloat(3f) + PmFloat(2f))).floatValue(), 0.001f)
        assertEquals(2.5f, (PmFloat(50f) / PmFloat(20f)).floatValue(), 0.001f)
    }

    @Test
    fun testComparisons() {
        assertTrue(PmFloat(200f) < PmFloat(201f))
        assertFalse(PmFloat(100f) < PmFloat(50f))
        assertTrue(PmFloat(300f) > PmFloat(299f))
        assertFalse(PmFloat(200f) > PmFloat(201f))
        assertTrue(PmFloat(100f) <= PmFloat(100f))
        assertFalse(PmFloat(101f) <= PmFloat(100f))
        assertTrue(PmFloat(100f) >= PmFloat(100f))
        assertFalse(PmFloat(99f) >= PmFloat(100f))
    }
}
