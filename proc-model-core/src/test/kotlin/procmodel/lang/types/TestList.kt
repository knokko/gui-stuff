package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TestList {

    @Test
    fun testBasic() {
        val list = PmList(mutableListOf(PmInt(5)))

        list.elements.add(PmInt(3))

        assertEquals(PmInt(5), list[PmInt(0)])
        assertEquals(PmInt(3), list[PmInt(1)])

        list[PmInt(1)] = PmInt(10)
        assertEquals(PmInt(10), list[PmInt(1)])
        assertEquals(PmInt(2), list.getProperty("size"))
    }

    @Test
    fun testEquals() {
        assertEquals(
                PmList(mutableListOf(PmString("hello"))),
                PmList(mutableListOf(PmString("hello")))
        )
        assertNotEquals(
                PmList(mutableListOf(PmString("Hello"))),
                PmList(mutableListOf(PmString("hello")))
        )
    }

    @Test
    fun testAdd() {
        assertEquals(
                PmList(mutableListOf(PmInt(3), PmInt(5))),
                PmList(mutableListOf(PmInt(3))) + PmList(mutableListOf(PmInt(5)))
        )
    }

    @Test
    fun testMultiply() {
        assertEquals(
                PmList(mutableListOf(
                        PmFloat(2f), PmFloat(3f), PmFloat(2f), PmFloat(3f),
                        PmFloat(2f), PmFloat(3f)
                )),
                PmInt(3) * PmList(mutableListOf(PmFloat(2f), PmFloat(3f)))
        )
        assertEquals(
                PmInt(3) * PmList(mutableListOf(PmFloat(2f), PmFloat(3f))),
                PmList(mutableListOf(PmFloat(2f), PmFloat(3f))) * PmInt(3)
        )
    }
}
