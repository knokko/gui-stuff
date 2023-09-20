package procmodel.program

import procmodel.lang.instructions.PmInstruction

/**
 * A `PmChildParameterConstructor` is a list of instructions that describes how to create the dynamic child parameter
 * map from the dynamic parameters of the parent model.
 *
 * Before executing the instructions, the dynamic parameters of the parent model should be declared as variables in the
 * root variable scope.
 *
 * The instructions use the built-in `outputValue` function to mark the result, which should be a `PmMap` where each key
 * should be a `PmString` with the name of the corresponding dynamic parameter.
 */
class PmChildParameterConstructor(instructions: List<PmInstruction>) : PmProgramBody(instructions) {
}