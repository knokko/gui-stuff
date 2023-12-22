package procmodel.program

import procmodel.lang.types.PmFatType
import procmodel.lang.types.PmType

class PmChildProgram(
    val instructionIndex: Int,
    val staticParameters: Map<String, PmType>,
    val dynamicParameters: Map<String, PmFatType>
) {
    override fun equals(other: Any?) = other is PmChildProgram && other.instructionIndex == this.instructionIndex &&
            other.staticParameters == this.staticParameters && other.dynamicParameters == this.dynamicParameters

    override fun hashCode(): Int {
        var result = instructionIndex
        result = 31 * result + staticParameters.hashCode()
        result = 31 * result + dynamicParameters.hashCode()
        return result
    }
}
