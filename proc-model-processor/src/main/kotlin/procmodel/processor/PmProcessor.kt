package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.functions.PmBuiltinFunction
import procmodel.lang.functions.PmBuiltinFunctions

import procmodel.lang.instructions.PmInstruction
import procmodel.lang.instructions.PmInstructionType
import procmodel.lang.types.*
import procmodel.program.PmProgramBody
import procmodel.scope.PmVariableScope
import kotlin.jvm.Throws

abstract class PmProcessor(
    private val body: PmProgramBody
) {
    protected var variables = PmVariableScope()
    protected val valueStack = mutableListOf<PmValue>()

    private val customBuiltinFunctions = mutableMapOf<String, PmBuiltinFunction>()

    fun addBuiltinFunction(name: String, function: PmBuiltinFunction) {
        customBuiltinFunctions[name] = function
    }

    @Throws(PmRuntimeError::class)
    protected open fun executeInstructions() {
        variables.pushScope()

        var instructionIndex = 0
        while (true) {
            val instruction = body.instructions[instructionIndex]
            instructionIndex += try {
                if (instruction.type == PmInstructionType.Jump) {
                    val jumpOffset = valueStack.removeLast().intValue()
                    val shouldJump = valueStack.removeLast().booleanValue()
                    if (shouldJump) jumpOffset else 1
                } else if (instruction.type == PmInstructionType.ExitProgram) {
                    val targetInstruction = handleExit()
                    if (targetInstruction == -1) break
                    else targetInstruction - instructionIndex
                } else if (instruction.type == PmInstructionType.CreateChildModel) {
                    val targetInstruction = handleChildModel(instruction, instructionIndex)
                    targetInstruction - instructionIndex
                } else {
                    executeInstruction(instruction)
                    1
                }
            } catch (error: PmRuntimeError) {
                throw PmRuntimeError(error.message!!, instruction.lineNumber)
            }
        }

        variables.popScope()
    }

    protected open fun handleExit(): Int = -1

    protected open fun handleChildModel(instruction: PmInstruction, currentInstructionIndex: Int): Int
            = throw PmRuntimeError("Child models are only supported by vertex processors")

    protected open fun executeInstruction(instruction: PmInstruction) {
        when (instruction.type) {
            PmInstructionType.PushValue -> valueStack.add(
                instruction.value?.copy() ?: instruction.variableType!!.createDefaultValue!!.invoke()
            )
            PmInstructionType.PushVariable -> valueStack.add(
                variables.getVariable(instruction.name!!) ?: throw PmRuntimeError("Undefined variable ${instruction.name}")
            )
            PmInstructionType.PushProperty -> valueStack.add(valueStack.removeLast().getProperty(instruction.name!!))
            PmInstructionType.ReadIndexed -> {
                val key = valueStack.removeLast()
                val map = valueStack.removeLast()
                valueStack.add(map[key])
            }

            PmInstructionType.Duplicate -> valueStack.add(valueStack.last())
            PmInstructionType.Swap -> {
                val x = valueStack.removeLast()
                val y = valueStack.removeLast()
                valueStack.add(x)
                valueStack.add(y)
            }
            PmInstructionType.Delete -> valueStack.removeLast()

            PmInstructionType.Divide -> { val right = valueStack.removeLast(); valueStack.add(valueStack.removeLast() / right) }
            PmInstructionType.Multiply -> { val right = valueStack.removeLast(); valueStack.add(valueStack.removeLast() * right) }
            PmInstructionType.Add -> { val right = valueStack.removeLast(); valueStack.add(valueStack.removeLast() + right) }
            PmInstructionType.Subtract -> { val right = valueStack.removeLast(); valueStack.add(valueStack.removeLast() - right) }

            PmInstructionType.SmallerThan -> {
                val right = valueStack.removeLast(); valueStack.add(PmBoolean(valueStack.removeLast() < right))
            }
            PmInstructionType.SmallerOrEqual -> {
                val right = valueStack.removeLast(); valueStack.add(PmBoolean(valueStack.removeLast() <= right))
            }

            PmInstructionType.DeclareVariable -> variables.defineVariable(
                instruction.variableType!!, instruction.name!!, valueStack.removeLast()
            )
            PmInstructionType.ReassignVariable -> variables.reassignVariable(instruction.name!!, valueStack.removeLast())
            PmInstructionType.SetProperty -> {
                val newValue = valueStack.removeLast()
                valueStack.removeLast().setProperty(instruction.name!!, newValue)
            }
            PmInstructionType.WriteIndexed -> {
                val value = valueStack.removeLast()
                val key = valueStack.removeLast()
                val map = valueStack.removeLast()
                map[key] = value
            }

            PmInstructionType.InvokeBuiltinFunction -> invokeBuiltinFunction(instruction.name!!)

            PmInstructionType.PushScope -> variables.pushScope()
            PmInstructionType.PopScope -> variables.popScope()

            else -> throw PmRuntimeError("Unknown instruction type ${instruction.type}")
        }
    }

    private fun invokeBuiltinFunction(name: String) {
        val builtinFunction = PmBuiltinFunctions.MAP[name] ?: customBuiltinFunctions[name]
            ?: throw PmRuntimeError("Unknown built-in function $name")
        builtinFunction.invoke(valueStack)
    }
}
