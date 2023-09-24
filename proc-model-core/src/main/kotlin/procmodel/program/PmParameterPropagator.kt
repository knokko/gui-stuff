package procmodel.program

import procmodel.lang.instructions.PmInstruction

/**
 * A `PmParameterPropagator` contains the instructions to propagate dynamic parameters from a parent model to a
 * dynamic matrix of a child model.
 *
 * The dynamic matrices of a model will simply use the dynamic parameters of that model, and no dynamic parameter
 * propagation is needed.
 *
 * ## Motivation
 * The situation becomes complicated when a parent model has a child model with dynamic matrices, since the child model
 * doesn't have its own dynamic parameters. Instead, the child model will somehow need to obtain the dynamic parameters
 * from the parent model. Since it may be desirable to give different dynamic parameters to different instances of the
 * same child model (e.g. left leg and right leg), simply copying the parameters from the parent model to the child
 * model is not sufficiently powerful.
 *
 * Instances of this class contain the instructions to *propagate* the dynamic parameters from the parent model to the
 * child model: given the dynamic parameters of the parent model (which are expected to be pre-defined as variables),
 * it creates a `PmMap` from `PmString`s to the dynamic parameter values of the child model, and marks it using
 * the built-in `outputValue` function.
 */
class PmParameterPropagator(instructions: List<PmInstruction>) : PmProgramBody(instructions) {
}