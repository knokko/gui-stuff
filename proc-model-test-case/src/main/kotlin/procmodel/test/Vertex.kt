package procmodel.test

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmColor
import procmodel.lang.types.PmMatrixIndex
import procmodel.lang.types.PmValue

class Vertex(var position: Position?, var color: PmColor, var matrix: PmMatrixIndex) : PmValue() {
    override fun copy() = Vertex(position, color, matrix)

    override fun getProperty(propertyName: String): PmValue {
        return when (propertyName) {
            "position" -> position ?: throw PmRuntimeError("Position hasn't been specified yet")
            "color" -> color
            "matrix" -> matrix
            else -> throw PmRuntimeError("Unknown property Vertex.$propertyName")
        }
    }

    override fun setProperty(propertyName: String, newValue: PmValue) {
        when (propertyName) {
            "position" -> position = newValue.castTo()
            "color" -> color = newValue.castTo()
            "matrix" -> matrix = newValue.castTo()
        }
    }

    override fun equals(other: Any?) = other is Vertex && other.position == this.position &&
            other.color == this.color && other.matrix == this.matrix

    override fun hashCode(): Int {
        var result = position?.hashCode() ?: 0
        result = 31 * result + color.hashCode()
        result = 31 * result + matrix.hashCode()
        return result
    }
}
