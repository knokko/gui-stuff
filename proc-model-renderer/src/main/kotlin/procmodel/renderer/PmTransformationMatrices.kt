package procmodel.renderer

import com.github.knokko.boiler.buffer.MappedVmaBuffer
import com.github.knokko.boiler.instance.BoilerInstance
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma.vmaDestroyBuffer
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkWriteDescriptorSet
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.functions.PmBuiltinFunction
import procmodel.lang.types.PmMap
import procmodel.lang.types.PmString
import procmodel.lang.types.PmType
import procmodel.lang.types.PmValue
import procmodel.processor.PmDynamicParameterProcessor
import procmodel.processor.PmMatrixProcessor
import procmodel.program.PmProgramBody
import procmodel.renderer.config.PmMatrixInfo
import procmodel.renderer.config.PmPipelineInfo

class PmTransformationMatrices<Matrix> internal constructor(
    private val boiler: BoilerInstance,
    private val matrixInfo: PmMatrixInfo<Matrix>,
    private val pipelineInfo: PmPipelineInfo<Matrix>,
    private val builtinFunctions: Map<String, PmBuiltinFunction>,
) {
    val descriptorSet = pipelineInfo.descriptorBank.borrowDescriptorSet()
    private var matrixBuffer: MappedVmaBuffer? = null

    private fun ensureMatrixBuffer(requiredSize: Int) {
        if (matrixBuffer == null || matrixBuffer!!.size < requiredSize) {
            if (matrixBuffer != null) {
                matrixBuffer!!.destroy(boiler.vmaAllocator())
            }

            // TODO Potentially use storage buffer instead
            // OPTIMIZE Use device-local buffer instead
            matrixBuffer = boiler.buffers.createMapped(
                requiredSize.toLong(), VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, "PmMatrixBuffer"
            )

            stackPush().use { stack ->
                val descriptorWrites = VkWriteDescriptorSet.calloc(1, stack)
                descriptorWrites.`sType$Default`()
                descriptorWrites.dstSet(descriptorSet)
                descriptorWrites.dstBinding(0)
                descriptorWrites.dstArrayElement(0)
                descriptorWrites.descriptorCount(1)
                descriptorWrites.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                descriptorWrites.pBufferInfo(boiler.descriptors.bufferInfo(stack, matrixBuffer!!.asBuffer()))

                vkUpdateDescriptorSets(boiler.vkDevice(), descriptorWrites, null)
            }
        }
    }

    @Throws(PmRuntimeError::class)
    fun computeAndStore(meshes: List<MeshDrawTask>) {
        val requiredNumMatrices = meshes.sumOf { it.mesh.matrices.size }
        ensureMatrixBuffer(requiredNumMatrices * matrixInfo.transformByteSize)

        val meshesWithMatrices = meshes.map { meshTask ->
            val mesh = meshTask.mesh
            val dynamicParameterValues = meshTask.dynamicParameters

            val matrices = ArrayList<Triple<Matrix, Map<String, PmValue>, Map<String, PmType>>>(mesh.matrices.size)
            for (matrix in mesh.matrices) {
                if (matrix != null) {
                    val (parentMatrix, dynamicParentParameterValues, dynamicParentParameterTypes) = matrices[matrix.parentIndex]

                    val childParameterValues = if (matrix.propagator != null) {
                        val parameterProcessor = PmDynamicParameterProcessor(
                            PmProgramBody(matrix.propagator!!.instructions),
                            dynamicParentParameterValues,
                            dynamicParentParameterTypes
                        )
                        for ((functionName, function) in builtinFunctions) {
                            parameterProcessor.addBuiltinFunction(functionName, function)
                        }
                        parameterProcessor.execute()

                        val pmResult = parameterProcessor.result as PmMap
                        val result = mutableMapOf<String, PmValue>()
                        for ((key, value) in pmResult.map) {
                            result[key.castTo<PmString>().value] = value
                        }
                        result
                    } else {
                        if (matrix.parentIndex != 0) throw PmRuntimeError("Parent index must be 0 when propagation instructions are null")
                        dynamicParentParameterValues
                    }

                    val childMatrixProcessor = PmMatrixProcessor(matrix, childParameterValues)
                    for ((functionName, function) in builtinFunctions) {
                        childMatrixProcessor.addBuiltinFunction(functionName, function)
                    }
                    childMatrixProcessor.execute()
                    val relativeMatrix = matrixInfo.takeResult(childMatrixProcessor.result!!)

                    val childMatrix = matrixInfo.multiply(parentMatrix, relativeMatrix)
                    matrices.add(Triple(childMatrix, childParameterValues, matrix.dynamicParameterTypes))
                } else matrices.add(Triple(matrixInfo.identity(), dynamicParameterValues, mesh.dynamicParameterTypes))
            }
            Pair(mesh, matrices)
        }

        var matrixIndex = 0
        val meshesWithMatrixIndices = mutableListOf<Pair<PmMesh, Int>>()
        val floatBuffer = MemoryUtil.memFloatBuffer(
            matrixBuffer!!.hostAddress,
            matrixInfo.transformByteSize * requiredNumMatrices
        )
        for ((mesh, matrices) in meshesWithMatrices) {
            meshesWithMatrixIndices.add(Pair(mesh, matrixIndex))
            for ((matrix, _, _) in matrices) {
                val bufferIndex = matrixInfo.transformByteSize * matrixIndex / Float.SIZE_BYTES
                matrixInfo.storeTransform(matrix, floatBuffer, bufferIndex)
                matrixIndex += 1
            }
        }
    }

    fun destroy() {
        pipelineInfo.descriptorBank.returnDescriptorSet(descriptorSet)
        if (matrixBuffer != null) {
            vmaDestroyBuffer(boiler.vmaAllocator(), matrixBuffer!!.vkBuffer, matrixBuffer!!.vmaAllocation)
        }
    }
}
