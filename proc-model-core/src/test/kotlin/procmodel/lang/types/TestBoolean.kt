package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TestBoolean {

    @Test
    fun testEquals() {
        assertEquals(PmBoolean(true), PmBoolean(true))
        assertEquals(PmBoolean(false), PmBoolean(false))
        assertNotEquals(PmBoolean(true), PmBoolean(false))
    }
}
