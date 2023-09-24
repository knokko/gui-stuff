package procmodel.processor

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.functions.PmBuiltinFunctions
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.instructions.PmInstructionType
import procmodel.lang.types.*
import procmodel.model.PmDynamicMatrix
import procmodel.model.PmModel
import procmodel.program.PmProgram
import procmodel.scope.PmVariableScope
import kotlin.jvm.Throws

class PmVertexProcessor<VertexValue : PmValue, Vertex>(
    private val program: PmProgram,
    rawStaticParameters: PmValue,
    private val vertexValueClass: Class<VertexValue>,
    private val finishVertex: (VertexValue) -> Vertex
): PmProcessor(program.body) {

    private val programStack = mutableListOf<ProgramEntry<VertexValue>>()

    private var vertices = mutableListOf<VertexValue>()
    private var dynamicMatrices = mutableListOf<PmDynamicMatrix?>(null)

    private var transferVariables = mutableMapOf<String, Pair<PmType, PmValue>>()

    init {
        initializeStaticParameters(rawStaticParameters, program.staticParameters)
    }

    @Throws(PmRuntimeError::class)
    fun execute(): PmModel<Vertex> {
        executeInstructions()
        variables.popScope() // Pop static parameter values

        if (vertices.isEmpty()) throw PmRuntimeError("Not a single triangle was produced")
        if (valueStack.isNotEmpty()) throw IllegalStateException("Value stack should be empty")
        if (variables.hasScope()) throw IllegalStateException("All scopes should have been popped")

        return PmModel(vertices.map(finishVertex), dynamicMatrices, program.dynamicParameters)
    }

    private fun initializeStaticParameters(rawStaticParameters: PmValue, staticParameters: Map<String, PmType>) {
        val staticParameterValues = mutableMapOf<String, PmValue>()
        if (staticParameters.isEmpty()) {
            if (rawStaticParameters is PmMap) {
                if (rawStaticParameters.map.isNotEmpty()) {
                    throw PmRuntimeError("No parameters needed, but ${rawStaticParameters.map.size} were specified")
                }
            } else if (rawStaticParameters !is PmNone) {
                throw PmRuntimeError("No parameters expected, but got $rawStaticParameters")
            }
        } else {
            if (rawStaticParameters !is PmMap) {
                throw PmRuntimeError("Expected a Map of parameters, but got $rawStaticParameters")
            }
            for (key in rawStaticParameters.map.keys) {
                if (key !is PmString) throw PmRuntimeError("All static parameter keys should be strings, but found $key")
                if (!staticParameters.containsKey(key.value)) throw PmRuntimeError("Unexpected static parameter $key")
            }

            for ((key, expectedType) in staticParameters) {
                val value = rawStaticParameters.map[PmString(key)] ?:
                throw PmRuntimeError("Missing static parameter $key")
                if (!expectedType.acceptValue(value)) {
                    throw PmRuntimeError("Type $expectedType of static parameter $key doesn't accept $value")
                }
                staticParameterValues[key] = value
            }
        }

        variables.pushScope()
        for ((key, value) in staticParameterValues) {
            val type = staticParameters[key] ?: throw PmRuntimeError("Unknown static parameter $key")
            variables.defineVariable(type, key, value.copy())
        }
    }

    override fun executeInstruction(instruction: PmInstruction) {
        when (instruction.type) {
            PmInstructionType.TransferVariable -> transferVariable(instruction)
            PmInstructionType.CreateDynamicMatrix -> createDynamicMatrix()

            else -> super.executeInstruction(instruction)
        }
    }

    override fun handleChildModel(instruction: PmInstruction, currentInstructionIndex: Int): Int {
        val modelID = instruction.name!!
        val childModel = program.children[modelID]!!

        val dynamicChildBlockIndex = valueStack.removeLast().castTo<PmInt>().intValue()
        val staticParameters = valueStack.removeLast()
        val parentMatrix = valueStack.removeLast().castTo<PmMatrixIndex>()

        programStack.add(ProgramEntry(
            currentInstructionIndex + 1, parentMatrix, dynamicChildBlockIndex, childModel.dynamicParameters,
            this.variables, this.vertices, this.dynamicMatrices, this.transferVariables
        ))

        this.variables = PmVariableScope()
        this.vertices = mutableListOf()
        this.dynamicMatrices = mutableListOf(null)
        this.transferVariables = mutableMapOf()

        this.initializeStaticParameters(staticParameters, childModel.staticParameters)

        return childModel.instructionIndex
    }

    override fun handleExit(): Int {
        val entry = programStack.removeLastOrNull() ?: return -1

        for ((index, childMatrix) in this.dynamicMatrices.withIndex()) {
            if (index == 0) {
                if (childMatrix != null) throw PmRuntimeError("First dynamic matrix must be null")
            } else {
                if (childMatrix == null) throw PmRuntimeError("Only first dynamic matrix must be null")
                if (childMatrix.parentIndex == 0) childMatrix.parentIndex = entry.parentMatrix.index
                else childMatrix.parentIndex += entry.dynamicMatrices.size - 1
            }
        }

        for (childVertex in this.vertices.toSet()) {
            val childMatrixIndex = childVertex.getProperty("matrix").castTo<PmMatrixIndex>().index
            if (childMatrixIndex == 0) childVertex.setProperty("matrix", entry.parentMatrix)
            else childVertex.setProperty("matrix", PmMatrixIndex(entry.dynamicMatrices.size + childMatrixIndex - 1))
        }

        this.variables.popScope()
        if (this.variables.hasScope()) throw PmRuntimeError("Unexpected variable scope left")

        this.variables = entry.variables
        entry.vertices.addAll(this.vertices)
        this.vertices = entry.vertices
        entry.dynamicMatrices.addAll(this.dynamicMatrices.subList(1, this.dynamicMatrices.size))
        this.dynamicMatrices = entry.dynamicMatrices
        this.transferVariables = entry.transferVariables

        return entry.returnInstructionIndex
    }

    private fun createDynamicMatrix() {
        val dynamicIndex = valueStack.removeLast().intValue()
        val matrixIndex = dynamicMatrices.size

        val programEntry = programStack.lastOrNull()
        val (childInvocation, dynamicParameterTypes) = if (programEntry != null) {
            Pair(program.childInvocations[programEntry.dynamicChildBlockIndex], programEntry.dynamicParameters)
        } else Pair(null, program.dynamicParameters)

        dynamicMatrices.add(PmDynamicMatrix(
            program.dynamicMatrices[dynamicIndex],
            childInvocation?.dynamicParameters,
            dynamicParameterTypes,
            transferVariables
        ))
        transferVariables = mutableMapOf()
        valueStack.add(PmMatrixIndex(matrixIndex))
    }

    private fun transferVariable(instruction: PmInstruction) {
        if (transferVariables.containsKey(instruction.name!!)) {
            throw PmRuntimeError("Duplicate transferred variable ${instruction.name}")
        }

        val variable = variables.getVariable(instruction.name!!) ?: throw PmRuntimeError("Unknown variable ${instruction.name}")
        transferVariables[instruction.name!!] = Pair(instruction.variableType!!, variable.copy())
    }

    override fun invokeBuiltinFunction(name: String) {
        when (name) {
            "produceTriangle" -> PmBuiltinFunctions.PRODUCE_TRIANGLE.invoke(valueStack) { parameters ->
                vertices.add(parameters[0].castTo(vertexValueClass))
                vertices.add(parameters[1].castTo(vertexValueClass))
                vertices.add(parameters[2].castTo(vertexValueClass))
                PmNone()
            }
            else -> super.invokeBuiltinFunction(name)
        }
    }

    private class ProgramEntry<VertexValue : PmValue>(
        val returnInstructionIndex: Int,
        val parentMatrix: PmMatrixIndex,
        val dynamicChildBlockIndex: Int,
        val dynamicParameters: Map<String, PmType>,
        val variables: PmVariableScope,
        val vertices: MutableList<VertexValue>,
        val dynamicMatrices: MutableList<PmDynamicMatrix?>,
        val transferVariables: MutableMap<String, Pair<PmType, PmValue>>
    )
}
