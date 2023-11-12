package procmodel2

import org.joml.Matrix3x2f
import procmodel.lang.types.PmType

object Pm2dTypes {
    val position = PmType("position", null, Pm2dPositionValue::class)
    val vertex = PmType("Vertex", { Pm2dVertexValue() }, Pm2dVertexValue::class)
    val matrix = PmType("Matrix", { Pm2dMatrixValue(Matrix3x2f()) }, Pm2dMatrixValue::class)

    val all = listOf(position, vertex, matrix)
}
