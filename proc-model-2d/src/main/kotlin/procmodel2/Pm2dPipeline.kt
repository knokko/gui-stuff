package procmodel2

import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.boiler.pipelines.GraphicsPipelineBuilder
import com.github.knokko.boiler.pipelines.ShaderInfo
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import procmodel.renderer.config.PmRenderPassInfo

internal fun createGraphicsPipeline(
    boiler: BoilerInstance,
    info: PmRenderPassInfo,
    pipelineLayout: Long,
    vertexSize: Int
): Long {
    return stackPush().use { stack ->
        val pipelineBuilder = GraphicsPipelineBuilder(boiler, stack)

        val vertexShader = boiler.pipelines.createShaderModule(
            stack, "procmodel2/shaders/simple.vert.spv", "Pm2VertexShader"
        )
        val fragmentShader = boiler.pipelines.createShaderModule(
            stack, "procmodel2/shaders/simple.frag.spv", "Pm2FragmentShader"
        )
        pipelineBuilder.shaderStages(
            ShaderInfo(VK_SHADER_STAGE_VERTEX_BIT, vertexShader, null),
            ShaderInfo(VK_SHADER_STAGE_FRAGMENT_BIT, fragmentShader, null)
        )
        pipelineBuilder.ciPipeline.pVertexInputState(createVertexInputState(stack, vertexSize))
        pipelineBuilder.simpleInputAssembly()
        pipelineBuilder.dynamicViewports(1)
        pipelineBuilder.simpleRasterization(VK_CULL_MODE_NONE) // TODO Cull back when switching to 3D
        pipelineBuilder.noDepthStencil() // TODO Use depthStencil after switching to 3D

        info.populateBlendState(pipelineBuilder.ciPipeline, stack)
        pipelineBuilder.dynamicStates(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR)
        pipelineBuilder.noMultisampling()
        pipelineBuilder.ciPipeline.layout(pipelineLayout)
        info.populateRenderPass(pipelineBuilder.ciPipeline, stack)

        val pipeline = pipelineBuilder.build("Pm2Pipeline")

        vkDestroyShaderModule(boiler.vkDevice(), vertexShader, null)
        vkDestroyShaderModule(boiler.vkDevice(), fragmentShader, null)

        pipeline
    }
}

private fun createVertexInputState(stack: MemoryStack, vertexSize: Int): VkPipelineVertexInputStateCreateInfo {
    val bindings = VkVertexInputBindingDescription.calloc(1, stack)
    val staticBinding = bindings[0]
    staticBinding.binding(0)
    staticBinding.stride(vertexSize)
    staticBinding.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)

    val attributes = VkVertexInputAttributeDescription.calloc(3, stack)
    val attributePosition = attributes[0]
    attributePosition.location(0)
    attributePosition.binding(0)
    attributePosition.format(VK_FORMAT_R32G32_SFLOAT)
    attributePosition.offset(0)

    val attributeColor = attributes[1]
    attributeColor.location(1)
    attributeColor.binding(0)
    attributeColor.format(VK_FORMAT_R32G32B32_SFLOAT)
    attributeColor.offset(8)

    val attributeMatrix = attributes[2]
    attributeMatrix.location(2)
    attributeMatrix.binding(0)
    attributeMatrix.format(VK_FORMAT_R32_SINT)
    attributeMatrix.offset(20)

    val ciVertexInput = VkPipelineVertexInputStateCreateInfo.calloc(stack)
    ciVertexInput.`sType$Default`()
    ciVertexInput.flags(0)
    ciVertexInput.pVertexBindingDescriptions(bindings)
    ciVertexInput.pVertexAttributeDescriptions(attributes)

    return ciVertexInput
}
