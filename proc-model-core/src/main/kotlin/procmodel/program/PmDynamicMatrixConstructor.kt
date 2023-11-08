package procmodel.program

import procmodel.lang.instructions.PmInstruction

/**
 * A `PmDynamicMatrixConstructor` is a list of instructions that uses the dynamic parameter values to create a matrix.
 *
 * Before executing the instructions, the dynamic parameters of the parent model should be declared as variables in the
 * root variable scope.
 *
 * The instructions use the built-in `outputValue` function to mark the result.
 */
class PmDynamicMatrixConstructor(instructions: List<PmInstruction>) : PmProgramBody(instructions) {
    override fun equals(other: Any?) = other is PmDynamicMatrixConstructor && other.instructions == this.instructions

    override fun hashCode() = instructions.hashCode()
}
