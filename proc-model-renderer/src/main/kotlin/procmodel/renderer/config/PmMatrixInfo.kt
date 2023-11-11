package procmodel.renderer.config

import procmodel.lang.types.PmValue
import java.nio.FloatBuffer

/**
 * ProcModel supports both 2d models and 3d models. However, such models require different transformation matrices
 * (typically 3x2 matrices for 2d and 4x3 matrices for 3d). This class captures these differences, allowing the rest of
 * ProcModel to be unaware of the layout of transformation matrices.
 */
class PmMatrixInfo<Matrix>(
    val multiply: (Matrix, Matrix) -> Matrix,
    val identity: () -> Matrix,
    val takeResult: (PmValue) -> Matrix,
    val transformByteSize: Int,
    val storeTransform: (Matrix, FloatBuffer, indexOffset: Int) -> Unit
)
