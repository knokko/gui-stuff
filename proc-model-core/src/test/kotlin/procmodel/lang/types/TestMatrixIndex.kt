package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TestMatrixIndex {

    @Test
    fun testEquals() {
        assertEquals(PmMatrixIndex(1), PmMatrixIndex(1))
        assertNotEquals(PmMatrixIndex(2), PmMatrixIndex(1))
        assertEquals(PmMatrixIndex(3), PmMatrixIndex(3).copy())
    }
}
