package procmodel.lang.types

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestSet {

    @Test
    fun testEquals() {
        assertEquals(
            PmSet(mutableSetOf(PmInt(5), PmInt(8))),
            PmSet(mutableSetOf(PmInt(8), PmInt(5)))
        )
        assertNotEquals(
            PmSet(mutableSetOf(PmInt(-5), PmInt(8))),
            PmSet(mutableSetOf(PmInt(8), PmInt(5)))
        )
    }

    @Test
    fun testCopy() {
        val original = PmSet(mutableSetOf(PmSet(mutableSetOf(PmString("hey")))))
        val copied = original.copy()
        assertEquals(original, copied)
        assertNotSame(original, copied)
        assertNotSame(original.elements.first(), copied.elements.first())
    }

    @Test
    fun testSize() {
        assertEquals(PmInt(1), PmSet(mutableSetOf(PmInt(5), PmInt(5))).getProperty("size"))
    }

    @Test
    fun testPlus() {
        assertEquals(
            PmSet(mutableSetOf(PmFloat(2f), PmFloat(5f), PmFloat(6f))),
            PmSet(mutableSetOf(PmFloat(5f), PmFloat(2f)))
                    + PmSet(mutableSetOf(PmFloat(2f), PmFloat((6f))))
        )
    }
}
