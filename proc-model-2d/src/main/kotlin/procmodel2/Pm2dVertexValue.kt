package procmodel2

import graviks2d.util.Color
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmColor
import procmodel.lang.types.PmMatrixIndex
import procmodel.lang.types.PmValue

class Pm2dVertexValue : PmValue() {

    private lateinit var position: Pm2dPositionValue
    private var color = PmColor(0f, 0f, 0f)
    private var matrix = PmMatrixIndex(0)

    override fun getProperty(propertyName: String) = when(propertyName) {
        "position" -> this.position
        "color" -> this.color
        "matrix" -> this.matrix
        else -> throw PmRuntimeError("Unexpected property: vertex.$propertyName")
    }

    override fun setProperty(propertyName: String, newValue: PmValue) {
        when(propertyName) {
            "position" -> this.position = newValue.castTo()
            "color" -> this.color = newValue.castTo()
            "matrix" -> this.matrix = newValue.castTo()
            else -> throw PmRuntimeError("Unexpected property: vertex.$propertyName")
        }
    }

    override fun toString(): String {
        val positionString = if (this::position.isInitialized) this.position.toString() else "undefined"
        return "Vertex($positionString, $color, matrix=${matrix.index})"
    }

    fun toVertex() = Pm2dVertex(
        x = position.getProperty("x").floatValue(),
        y = position.getProperty("y").floatValue(),
        color = Color.rgbFloat(
            color.getProperty("red").floatValue(),
            color.getProperty("green").floatValue(),
            color.getProperty("blue").floatValue()
        ),
        matrixIndex = matrix.index
    )

    override fun copy(): PmValue {
        val copy = Pm2dVertexValue()
        if (this::position.isInitialized) copy.position = this.position
        copy.color = this.color
        copy.matrix = this.matrix
        return copy
    }
}
