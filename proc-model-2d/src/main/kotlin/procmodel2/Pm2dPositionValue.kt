package procmodel2

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmFloat
import procmodel.lang.types.PmValue

class Pm2dPositionValue(private val x: Float, private val y: Float) : PmValue() {

    override fun setProperty(propertyName: String, newValue: PmValue) {
        throw PmRuntimeError("Positions are immutable")
    }

    override fun getProperty(propertyName: String): PmValue {
        return when(propertyName) {
            "x" -> PmFloat(x)
            "y" -> PmFloat(y)
            else -> throw PmRuntimeError("Unknown property: position.$propertyName")
        }
    }

    override fun toString() = "PositionValue($x, $y)"

    override fun copy() = this
}
