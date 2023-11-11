package procmodel2

import graviks2d.util.Color

class Pm2dVertex(
    val x: Float,
    val y: Float,
    val color: Color,
    val matrixIndex: Int
) {
    override fun toString() = "Vertex($x, $y, $color, matrix=$matrixIndex)"
}
