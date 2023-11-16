package procmodel.lang.instructions

enum class PmInstructionType {

    PushValue,
    PushVariable,
    PushProperty,
    ReadIndexed,

    Divide,
    Multiply,
    Add,
    Subtract,

    SmallerThan,
    SmallerOrEqual,

    Duplicate,
    Swap,
    Delete,

    DeclareVariable,
    ReassignVariable,
    SetProperty,
    WriteIndexed,

    Jump,
    InvokeBuiltinFunction,
    TransferVariable,
    CreateDynamicMatrix,
    CreateChildModel,
    ExitProgram,

    PushScope,
    PopScope
}
