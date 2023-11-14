package procmodel.lang.types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestInt {

    @Test
    fun testIntValue() {
        assertEquals(-25, PmInt(-25).intValue())
    }

    @Test
    fun testEquals() {
        assertEquals(PmInt(123), PmInt(123))
        assertNotEquals(PmInt(123), PmInt(124))
    }

    @Test
    fun testArithmetic() {
        assertEquals(10, (PmInt(2) * (PmInt(3) + PmInt(2))).intValue())
        assertEquals(2, (PmInt(50) / PmInt(20)).intValue())
    }

    @Test
    fun testComparisons() {
        assertTrue(PmInt(200) < PmInt(201))
        assertFalse(PmInt(100) < PmInt(50))
        assertTrue(PmInt(300) > PmInt(299))
        assertFalse(PmInt(200) > PmInt(201))
        assertTrue(PmInt(100) <= PmInt(100))
        assertFalse(PmInt(101) <= PmInt(100))
        assertTrue(PmInt(100) >= PmInt(100))
        assertFalse(PmInt(99) >= PmInt(100))
    }
}
