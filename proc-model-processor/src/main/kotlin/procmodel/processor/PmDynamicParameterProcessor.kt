package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.*
import procmodel.program.PmProgramBody

class PmDynamicParameterProcessor(
    body: PmProgramBody,
    private val dynamicParentParameterValues: Map<String, PmValue>,
    private val dynamicParentParameterTypes: Map<String, PmFatType>
): PmValueProcessor(body) {

    override fun executeInstructions() {
        initializeParameters(
            variables, "dynamic",
            dynamicParentParameterTypes.mapValues { it.value.type },
            dynamicParentParameterValues
        )
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
