package procmodel.lang.instructions

enum class PmInstructionType {

    PushValue,
    PushVariable,
    PushProperty,
    ReadListOrMap,

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
    UpdateListOrMap,

    Jump,
    InvokeBuiltinFunction,
    TransferVariable,
    CreateDynamicMatrix,
    CreateChildModel,
    ExitProgram,

    PushScope,
    PopScope
}
