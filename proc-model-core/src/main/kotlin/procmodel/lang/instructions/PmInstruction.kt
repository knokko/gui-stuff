package procmodel.lang.instructions

import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue

class PmInstruction private constructor(
    val type: PmInstructionType,
    val lineNumber: Int,
    /**
     * When *type* is `PushValue`, this is the value to be pushed, or null if the default value of *variableType*
     * should be pushed. For all other instructions, this must be null.
     */
    val value: PmValue? = null,
    /**
     * When *type* is `DeclareVariable` or `TransferVariable`, this is the type of the variable to be declared.
     * For all other instructions, this must be null.
     */
    val variableType: PmType? = null,
    /**
     * - When *type* is `PushVariable`, `DeclareVariable`, `ReassignVariable`, or `TransferVariable`,
     * this is the name of the variable.
     * - When *type* is `PushProperty` or `SetProperty`, this is the name of the property.
     * - When *type* is `InvokeBuiltinFunction`, this is the name of the function
     * - When *type* is `AssignParameter`, this is the name of the parameter
     * - When *type* is `CreateChildModel`, this is the id of the child model
     * - For all other instructions, this must be null.
     */
    val name: String? = null
) {
    companion object {
        fun pushValue(value: PmValue, lineNumber: Int) = PmInstruction(
            PmInstructionType.PushValue, lineNumber, value = value
        )

        private fun named(type: PmInstructionType, name: String, lineNumber: Int) = PmInstruction(
            type, lineNumber, name = name
        )

        fun pushVariable(name: String, lineNumber: Int) = named(PmInstructionType.PushVariable, name, lineNumber)

        fun pushProperty(name: String, lineNumber: Int) = named(PmInstructionType.PushProperty, name, lineNumber)

        // TODO Find a better name for this
        fun readListOrMap(lineNumber: Int) = PmInstruction(PmInstructionType.ReadListOrMap, lineNumber)

        fun divide(lineNumber: Int) = PmInstruction(PmInstructionType.Divide, lineNumber)

        fun multiply(lineNumber: Int) = PmInstruction(PmInstructionType.Multiply, lineNumber)

        fun add(lineNumber: Int) = PmInstruction(PmInstructionType.Add, lineNumber)

        fun subtract(lineNumber: Int) = PmInstruction(PmInstructionType.Subtract, lineNumber)

        fun smallerThan(lineNumber: Int) = PmInstruction(PmInstructionType.SmallerThan, lineNumber)

        fun smallerOrEqual(lineNumber: Int) = PmInstruction(PmInstructionType.SmallerOrEqual, lineNumber)

        fun duplicate(lineNumber: Int) = PmInstruction(PmInstructionType.Duplicate, lineNumber)

        fun swap(lineNumber: Int) = PmInstruction(PmInstructionType.Swap, lineNumber)

        fun delete(lineNumber: Int) = PmInstruction(PmInstructionType.Delete, lineNumber)

        fun declareVariable(name: String, type: PmType, lineNumber: Int) = PmInstruction(
            PmInstructionType.DeclareVariable, lineNumber, variableType = type, name = name
        )

        fun reassignVariable(name: String, lineNumber: Int) = named(PmInstructionType.ReassignVariable, name, lineNumber)

        fun setProperty(name: String, lineNumber: Int) = named(PmInstructionType.SetProperty, name, lineNumber)

        // TODO Find a better name for this
        fun updateListOrMap(lineNumber: Int) = PmInstruction(PmInstructionType.UpdateListOrMap, lineNumber)

        fun jump(lineNumber: Int) = PmInstruction(PmInstructionType.Jump, lineNumber)

        fun invokeBuiltinFunction(name: String, lineNumber: Int) = named(PmInstructionType.InvokeBuiltinFunction, name, lineNumber)

        fun transferVariable(name: String, type: PmType, lineNumber: Int) = PmInstruction(
            PmInstructionType.TransferVariable, lineNumber, variableType = type, name = name
        )

        fun createDynamicMatrix(lineNumber: Int) = PmInstruction(
            PmInstructionType.CreateDynamicMatrix, lineNumber
        )

        fun createChildModel(id: String, lineNumber: Int) = named(PmInstructionType.CreateChildModel, id, lineNumber)

        fun exitProgram(lineNumber: Int) = PmInstruction(PmInstructionType.ExitProgram, lineNumber)

        fun pushScope(lineNumber: Int) = PmInstruction(PmInstructionType.PushScope, lineNumber)

        fun popScope(lineNumber: Int) = PmInstruction(PmInstructionType.PopScope, lineNumber)
    }
}
