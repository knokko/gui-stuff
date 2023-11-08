package procmodel.program

import procmodel.lang.types.PmType

/**
 * A `Pm2Program` contains all the instructions and information to create a `PmModel`, given the necessary static
 * parameter values.
 */
class PmProgram(
    /**
     * The instructions to create the *static* part of the model: the vertices.
     */
    val body: PmProgramBody,
    /**
     * The element with index N contains the instructions to construct dynamic matrix N.
     */
    val dynamicMatrices: List<PmDynamicMatrixConstructor>,

    /**
     * The element with index N contains the instructions to propagate our dynamic parameters to the
     * dynamic parameters of child program N
     */
    val childInvocations: List<PmParameterPropagator>,

    /**
     * The imported child programs that are embedded in this program. The id (currently just the import path) of the
     * child program is used as key.
     *
     * For each child, there can be 0 or more *invocations* (if there are 0 invocations, the child model import is
     * however useless).
     */
    val children: Map<String, PmChildProgram>,
    val staticParameters: Map<String, PmType>,
    val dynamicParameters: Map<String, PmType>
) {
}
