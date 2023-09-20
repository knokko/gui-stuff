package procmodel.program

import procmodel.lang.instructions.PmInstruction
import procmodel.lang.instructions.PmInstructionType

/**
 * The *body* of a PM program, or a fragment of one. A body is a list of instructions ending with an `ExitProgram`
 * instruction.
 */
open class PmProgramBody(val instructions: List<PmInstruction>) {

    init {
        if (instructions.isEmpty()) throw IllegalArgumentException("PmProgramBody can't be empty")
        val lastInstruction = instructions.last()
        if (lastInstruction.type != PmInstructionType.ExitProgram) {
            throw IllegalArgumentException("PmProgramBody must end with an ExitProgram instruction")
        }
    }
}
