package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.types.PmMap
import procmodel.lang.types.PmString
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue
import procmodel.program.PmProgramBody

internal class PmDynamicParameterProcessor(
    body: PmProgramBody,
    private val dynamicParentParameterValues: Map<String, PmValue>,
    private val dynamicParentParameterTypes: Map<String, PmType>
): PmValueProcessor(body) {

    override fun executeInstructions() {
        initializeParameters(variables, "dynamic", dynamicParentParameterTypes, dynamicParentParameterValues)
        super.executeInstructions()
        variables.popScope()
    }

    override fun execute() {
        super.execute()

        val finalResult = result ?: throw PmRuntimeError("The outputValue function wasn't called")
        if (finalResult !is PmMap) throw PmRuntimeError("The result must be a PmMap, but is $finalResult")
        for (key in finalResult.map.keys) {
            if (key !is PmString) throw PmRuntimeError("All keys in the result map must be strings, but found $key")
        }
    }
}
