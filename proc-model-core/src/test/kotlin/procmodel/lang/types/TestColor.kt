package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import procmodel.exceptions.PmRuntimeError

class TestColor {

    @Test
    fun testConstructor() {
        val margin = 0.02f
        val testColor = PmColor(0f, 0.5f, 1f)
        assertEquals(0f, testColor.getProperty("red").floatValue(), margin)
        assertEquals(0.5f, testColor.getProperty("green").floatValue(), margin)
        assertEquals(1f, testColor.getProperty("blue").floatValue())

        assertThrows<PmRuntimeError> { PmColor(-0.1f, 0f, 1f) }
        assertThrows<PmRuntimeError> { PmColor(0f, 1.1f, 1f) }
    }

    @Test
    fun testEquals() {
        assertEquals(PmColor(0.1f, 0.4f, 0.8f), PmColor(0.1f, 0.4f, 0.8f))
        assertNotEquals(PmColor(0f, 0.4f, 0.8f), PmColor(0.1f, 0.4f, 0.8f))
        assertNotEquals(PmColor(0.1f, 0.4f, 0.8f), PmColor(0.1f, 0.5f, 0.8f))
        assertNotEquals(PmColor(0.1f, 0.4f, 1f), PmColor(0.1f, 0.4f, 0.8f))
    }
}
