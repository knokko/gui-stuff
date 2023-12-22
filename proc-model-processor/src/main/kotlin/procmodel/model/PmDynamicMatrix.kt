package procmodel.model

import procmodel.lang.types.PmFatType
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue
import procmodel.program.PmDynamicMatrixConstructor
import procmodel.program.PmParameterPropagator

/**
 * A `PmDynamicMatrix` contains the instructions to generate a dynamic transformation matrix of a `PmModel` and
 * `PmMesh`.
 *
 * It is dynamic in the sense that it is typically recomputed every frame, and that the resulting matrix typically
 * depends on the values of the *dynamic* parameters.
 *
 * Dynamic matrices are usually used to describe animations, and each vertex typically has 1 `matrixIndex` attribute
 * that references a dynamic matrix.
 */
class PmDynamicMatrix(
    val construction: PmDynamicMatrixConstructor,
    val propagator: PmParameterPropagator?,
    val dynamicParameterTypes: Map<String, PmFatType>,
    val transferredVariables: Map<String, Pair<PmType, PmValue>>
) {
    /**
     * The index of the parent matrix of this dynamic matrix, into the array of transformation matrices of the
     * `PmModel` (and the `PmMesh`).
     *
     * The index 0 is reserved for the identity matrix.
     */
    var parentIndex = 0

    override fun toString() =
        "DynamicMatrix(${construction.instructions.size} instructions, parent=$parentIndex, transfers $transferredVariables)"
}
