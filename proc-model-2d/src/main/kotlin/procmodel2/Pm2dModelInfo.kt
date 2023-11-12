package procmodel2

import com.github.knokko.boiler.exceptions.VulkanFailureException.assertVkSuccess
import com.github.knokko.boiler.instance.BoilerInstance
import org.joml.Matrix3x2f
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo
import org.lwjgl.vulkan.VkDescriptorPoolSize
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding
import org.lwjgl.vulkan.VkPushConstantRange
import procmodel.renderer.config.PmMatrixInfo
import procmodel.renderer.config.PmModelInfo
import procmodel.renderer.config.PmPipelineInfo
import procmodel.renderer.config.PmVertexInfo

fun createModelInfo2d(boiler: BoilerInstance): PmModelInfo<Pm2dVertex, Matrix3x2f> {
    val (pipelineLayout, descriptorSetLayout) = createLayout(boiler)
    val vertexSize = 24

    val vertexInfo = PmVertexInfo<Pm2dVertex>(
        byteSize = vertexSize,
        populate = { vertex, byteBuffer ->
            byteBuffer.putFloat(vertex.x)
            byteBuffer.putFloat(vertex.y)
            byteBuffer.putFloat(vertex.color.redF)
            byteBuffer.putFloat(vertex.color.greenF)
            byteBuffer.putFloat(vertex.color.blueF)
            byteBuffer.putInt(vertex.matrixIndex)
        }
    )

    val matrixInfo = PmMatrixInfo(
        multiply = { left, right -> left.mul(right, Matrix3x2f()) },
        identity = { Matrix3x2f() },
        takeResult = { value -> value.castTo<Pm2dMatrixValue>().matrix },
        transformByteSize = 48, // Due to stupid block alignment rules, this is larger than it needs to be
        storeTransform = { matrix, buffer, indexOffset ->
            buffer.put(indexOffset, matrix.m00)
            buffer.put(indexOffset + 1, matrix.m01)
            buffer.put(indexOffset + 4, matrix.m10)
            buffer.put(indexOffset + 5, matrix.m11)
            buffer.put(indexOffset + 8, matrix.m20)
            buffer.put(indexOffset + 9, matrix.m21)
        }
    )

    val pipelineInfo = PmPipelineInfo<Matrix3x2f>(
        pipelineLayout = pipelineLayout,
        createGraphicsPipeline = { pipelineInfo -> createGraphicsPipeline(
            boiler, pipelineInfo, pipelineLayout, vertexSize
        ) },
        descriptorSetLayout = descriptorSetLayout,
        createDescriptorPool = { amount ->
            stackPush().use { stack ->
                val descriptorPoolSizes = VkDescriptorPoolSize.calloc(1, stack)
                descriptorPoolSizes.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                descriptorPoolSizes.descriptorCount(1)

                val ciDescriptorPool = VkDescriptorPoolCreateInfo.calloc(stack)
                ciDescriptorPool.`sType$Default`()
                ciDescriptorPool.flags(0)
                ciDescriptorPool.maxSets(amount)
                ciDescriptorPool.pPoolSizes(descriptorPoolSizes)

                val pDescriptorPool = stack.callocLong(1)
                assertVkSuccess(vkCreateDescriptorPool(
                    boiler.vkDevice(), ciDescriptorPool, null, pDescriptorPool
                ), "CreateDescriptorPool", "Pm2Descriptors")
                val descriptorPool = pDescriptorPool[0]
                boiler.debug.name(stack, descriptorPool, VK_OBJECT_TYPE_DESCRIPTOR_POOL, "Pm2Descriptors")
                descriptorPool
            }
        },
        pushCameraMatrix = { cameraMatrix, stack, commandBuffer ->
            val matrixBuffer = stack.callocFloat(3 * 2)
            cameraMatrix.get(0, matrixBuffer)
            vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, matrixBuffer)
        },
        pushMatrixIndexOffset = { pushBuffer, commandBuffer ->
            vkCmdPushConstants(
                commandBuffer,
                pipelineLayout,
                VK_SHADER_STAGE_VERTEX_BIT,
                2 * 3 * 4,
                pushBuffer
            )
        }
    )

    return PmModelInfo(
        vertices = vertexInfo,
        matrices = matrixInfo,
        pipeline = pipelineInfo,
        builtinFunctions = Pm2dBuiltinFunctions.all,
    )
}

private fun createLayout(boiler: BoilerInstance) = stackPush().use { stack ->
    val descriptorBindings = VkDescriptorSetLayoutBinding.calloc(1, stack)
    descriptorBindings.binding(0)
    descriptorBindings.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
    descriptorBindings.descriptorCount(1)
    descriptorBindings.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)

    val descriptorSetLayout = boiler.descriptors.createLayout(stack, descriptorBindings, "Pm2DescriptorSetLayout")

    val pushConstants = VkPushConstantRange.calloc(1, stack)
    val pushCameraMatrix = pushConstants[0]
    pushCameraMatrix.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
    pushCameraMatrix.offset(0)
    pushCameraMatrix.size(4 + 3 * 2 * 4) // 1 int and 1 3x2 matrix

    val pipelineLayout = boiler.pipelines.createLayout(stack, pushConstants, "Pm2PipelineLayout", descriptorSetLayout)

    Pair(pipelineLayout, descriptorSetLayout)
}
