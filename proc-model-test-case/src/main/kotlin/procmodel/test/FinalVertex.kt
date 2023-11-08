package procmodel.test

import procmodel.lang.types.PmColor

class FinalVertex(val position: Position, val color: PmColor, val matrixIndex: Int) {
    constructor(vertex: Vertex) : this(vertex.position!!, vertex.color, vertex.matrix.index)

    override fun toString() = "Vertex($position, $color, $matrixIndex)"

    override fun equals(other: Any?) = other is FinalVertex && position == other.position &&
            color == other.color && matrixIndex == other.matrixIndex

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + matrixIndex
        return result
    }
}