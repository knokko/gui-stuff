package procmodel.program

import procmodel.lang.types.PmType

class PmChildProgram(
    val instructionIndex: Int,
    val staticParameters: Map<String, PmType>,
    val dynamicParameters: Map<String, PmType>
)
