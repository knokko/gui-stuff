package procmodel.test

import procmodel.lang.types.PmValue

class Position(val x: Float, val y: Float) : PmValue() {
    override fun copy() = this

    override fun toString() = "($x, $y)"

    override fun equals(other: Any?) = other is Position && x == other.x && y == other.y

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
