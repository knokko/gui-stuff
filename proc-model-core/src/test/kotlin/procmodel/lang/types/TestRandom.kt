package procmodel.lang.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

class TestRandom {

    @Test
    fun testNextFloat() {
        val seed = 1234L
        val rng = Random(seed)
        val pmRandom = PmRandom(Random(seed))

        for (counter in 0 until 10) {
            assertEquals(rng.nextFloat(), pmRandom.getProperty("nextFloat").floatValue())
        }
    }

    @Test
    fun testCopy() {
        val r1 = PmRandom(Random(123L))
        for (counter in 0 until 10) r1.getProperty("nextFloat")

        val r2 = r1.copy()
        for (counter in 0 until 10) {
            assertEquals(r1.getProperty("nextFloat"), r2.getProperty("nextFloat"))
        }
    }
}
