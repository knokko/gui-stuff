package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TestString {

    @Test
    fun testEquals() {
        assertEquals(PmString("hello world"), PmString("hello world"))
        assertNotEquals(PmString("Hello World"), PmString("hello world"))
        assertEquals(PmString("hello"), PmString("hello").copy())
    }
}
