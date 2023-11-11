package procmodel2

import org.joml.Matrix3x2f
import procmodel.lang.types.PmValue

class Pm2dMatrixValue(internal val matrix: Matrix3x2f): PmValue() {
    override fun copy() = Pm2dMatrixValue(Matrix3x2f(matrix))

    override fun toString() = "MutableMatrix($matrix)"
}
