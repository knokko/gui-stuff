package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TestNone {

    @Test
    fun testEquals() {
        assertEquals(PmNone(), PmNone())
        assertEquals(PmNone(), PmNone().copy())
        assertNotEquals(PmNone(), PmList(mutableListOf()))
    }
}
