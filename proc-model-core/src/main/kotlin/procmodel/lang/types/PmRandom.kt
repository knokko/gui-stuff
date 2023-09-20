package procmodel.lang.types

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.random.Random

class PmRandom(private val rng: Random) : PmValue() {

    override fun getProperty(propertyName: String): PmValue {
        return when (propertyName) {
            "nextFloat" -> PmFloat(rng.nextFloat())
            else -> super.getProperty(propertyName)
        }
    }

    override fun copy(): PmRandom {
        // Ripped from https://stackoverflow.com/a/54156572
        // Uses serialization to create a copy of the given Random, needed for repeatability in some tests.
        val bo = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bo)
        oos.writeObject(rng)
        oos.close()
        val ois = ObjectInputStream(ByteArrayInputStream(bo.toByteArray()))
        return PmRandom(ois.readObject() as Random)
    }

    override fun toString() = "PmRandom"
}
