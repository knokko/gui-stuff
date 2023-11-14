package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import procmodel.exceptions.PmRuntimeError

class TestMap {

    @Test
    fun testBasic() {
        val map1 = PmMap()
        val map2 = PmMap()

        assertEquals(map1, map2)

        map1[PmFloat(5f)] = PmString("hello")
        assertEquals(PmString("hello"), map1[PmFloat(5f)])
        assertNotEquals(map1, map2)

        map1[PmFloat(4f)] = PmInt(4)
        assertEquals(PmInt(4), map1[PmFloat(4f)])
        assertEquals(PmString("hello"), map1[PmFloat(5f)])

        map2[PmFloat(4f)] = PmInt(4)
        map2[PmFloat(5f)] = PmString("hello")
        assertEquals(map1, map2)
    }

    @Test
    fun testUnknownKeyThrowsException() {
        val map = PmMap()
        assertThrows<PmRuntimeError> { map[PmInt(6)] }
        map[PmString("nope")] = PmInt(6)
        assertThrows<PmRuntimeError> { map[PmInt(6)] }
    }
}
