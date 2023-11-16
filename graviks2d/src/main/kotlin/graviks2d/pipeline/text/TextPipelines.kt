package graviks2d.pipeline.text

import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.boiler.pipelines.GraphicsPipelineBuilder
import com.github.knokko.boiler.pipelines.ShaderInfo
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding

const val TEXT_COLOR_FORMAT = VK_FORMAT_R8_UNORM

internal class TextPipeline(
    val boiler: BoilerInstance
) {
    val countPipeline: Long
    private val countPipelineLayout: Long
    val oddPipeline: Long
    val oddPipelineLayout: Long
    val oddDescriptorSetLayout: Long
    val vkRenderPass: Long

    init {
        stackPush().use { stack ->

            val oddLayoutBindings = VkDescriptorSetLayoutBinding.calloc(1, stack)
            val oddLayoutBinding = oddLayoutBindings[0]
            oddLayoutBinding.binding(0)
            oddLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT)
            oddLayoutBinding.descriptorCount(1)
            oddLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)

            this.countPipelineLayout = boiler.pipelines.createLayout(stack, null, "GraviksTextCount")
            this.oddDescriptorSetLayout = boiler.descriptors.createLayout(stack, oddLayoutBindings, "GraviksTextOdd")
            this.oddPipelineLayout = boiler.pipelines.createLayout(stack, null ,"GraviksTextOdd", oddDescriptorSetLayout)
            val renderPass = createTextRenderPass(boiler, stack)
            this.vkRenderPass = renderPass

            val textCountVertexShader = boiler.pipelines.createShaderModule(
                stack, "graviks2d/shaders/textCount.vert.spv", "TextCountVertex"
            )
            val textCountFragmentShader = boiler.pipelines.createShaderModule(
                stack, "graviks2d/shaders/textCount.frag.spv", "TextCountFragment"
            )

            val countPipelineBuilder = GraphicsPipelineBuilder(boiler, stack)

            // Note: the following is shared with the regular graviks pipeline:
            // inputAssembly, viewportState, dynamicState, rasterizationState
            countPipelineBuilder.shaderStages(
                ShaderInfo(VK_SHADER_STAGE_VERTEX_BIT, textCountVertexShader, null),
                ShaderInfo(VK_SHADER_STAGE_FRAGMENT_BIT, textCountFragmentShader, null)
            )
            countPipelineBuilder.ciPipeline.pVertexInputState(createTextCountPipelineVertexInput(stack))
            countPipelineBuilder.simpleInputAssembly()
            countPipelineBuilder.dynamicViewports(1)
            countPipelineBuilder.dynamicStates(
                VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR
            )
            countPipelineBuilder.simpleRasterization(VK_CULL_MODE_NONE)
            // Note: instead of using multisampling, the text renderer simply claims bigger space for some characters and
            // downscales them when drawing
            countPipelineBuilder.noMultisampling()
            countPipelineBuilder.ciPipeline.pColorBlendState(createTextCountPipelineColorBlend(stack))
            countPipelineBuilder.ciPipeline.layout(this.countPipelineLayout)
            countPipelineBuilder.ciPipeline.renderPass(vkRenderPass)
            countPipelineBuilder.ciPipeline.subpass(0)

            val textOddVertexShader = boiler.pipelines.createShaderModule(
                stack, "graviks2d/shaders/textOdd.vert.spv", "TextOddVertex"
            )
            val textOddFragmentShader = boiler.pipelines.createShaderModule(
                stack, "graviks2d/shaders/textOdd.frag.spv", "TextOddFragment"
            )

            val oddPipelineBuilder = GraphicsPipelineBuilder(boiler, stack)
            oddPipelineBuilder.shaderStages(
                ShaderInfo(VK_SHADER_STAGE_VERTEX_BIT, textOddVertexShader, null),
                ShaderInfo(VK_SHADER_STAGE_FRAGMENT_BIT, textOddFragmentShader, null)
            )
            oddPipelineBuilder.ciPipeline.pVertexInputState(createTextOddPipelineVertexInput(stack))
            oddPipelineBuilder.simpleInputAssembly()
            oddPipelineBuilder.dynamicViewports(1)
            oddPipelineBuilder.dynamicStates(
                VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR
            )
            oddPipelineBuilder.simpleRasterization(VK_CULL_MODE_NONE)
            oddPipelineBuilder.noMultisampling()
            oddPipelineBuilder.ciPipeline.pColorBlendState(createTextOddPipelineColorBlend(stack))
            oddPipelineBuilder.ciPipeline.layout(this.oddPipelineLayout)
            oddPipelineBuilder.ciPipeline.renderPass(this.vkRenderPass)
            oddPipelineBuilder.ciPipeline.subpass(1)

            this.countPipeline = countPipelineBuilder.build("TextCountPipeline")
            this.oddPipeline = oddPipelineBuilder.build("TextOddPipeline")

            vkDestroyShaderModule(boiler.vkDevice(), textCountVertexShader, null)
            vkDestroyShaderModule(boiler.vkDevice(), textCountFragmentShader, null)
            vkDestroyShaderModule(boiler.vkDevice(), textOddVertexShader, null)
            vkDestroyShaderModule(boiler.vkDevice(), textOddFragmentShader, null)
        }
    }

    fun destroy() {
        vkDestroyPipeline(boiler.vkDevice(), this.countPipeline, null)
        vkDestroyPipelineLayout(boiler.vkDevice(), this.countPipelineLayout, null)
        vkDestroyPipeline(boiler.vkDevice(), this.oddPipeline, null)
        vkDestroyPipelineLayout(boiler.vkDevice(), this.oddPipelineLayout, null)
        vkDestroyDescriptorSetLayout(boiler.vkDevice(), this.oddDescriptorSetLayout, null)
        vkDestroyRenderPass(boiler.vkDevice(), this.vkRenderPass, null)
    }
}
