package procmodel.lang.types

import graviks2d.util.Color
import procmodel.exceptions.PmRuntimeError

class PmColor(private val red: Float, private val green: Float, private val blue: Float) : PmValue() {

    init {
        if (red < 0f || red > 1f) throw PmRuntimeError("Red ($red) must be in range [0, 1]")
        if (green < 0f || green > 1f) throw PmRuntimeError("Green ($green) must be in range [0, 1]")
        if (blue < 0f || blue > 1f) throw PmRuntimeError("Blue ($blue) must be in range [0, 1]")
    }

    constructor(color: Color) : this(color.redF, color.greenF, color.blueF)

    override fun setProperty(propertyName: String, newValue: PmValue) {
        throw PmRuntimeError("Colors are immutable")
    }

    override fun getProperty(propertyName: String): PmValue {
        return when(propertyName) {
            "red" -> PmFloat(red)
            "green" -> PmFloat(green)
            "blue" -> PmFloat(blue)
            else -> throw PmRuntimeError("Unknown property: color.$propertyName")
        }
    }

    override fun toString() = "rgb($red, $green, $blue)"

    override fun copy() = this
}
