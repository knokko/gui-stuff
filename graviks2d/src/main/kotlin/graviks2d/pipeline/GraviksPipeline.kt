package graviks2d.pipeline

import com.github.knokko.boiler.pipelines.GraphicsPipelineBuilder
import com.github.knokko.boiler.pipelines.ShaderInfo
import graviks2d.context.TARGET_COLOR_FORMAT
import graviks2d.core.GraviksInstance
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding


internal class GraviksPipeline(
    val instance: GraviksInstance
) {

    val vkPipeline: Long
    val vkPipelineLayout: Long
    val vkDescriptorSetLayout: Long
    val vkRenderPass: Long

    init {
        stackPush().use { stack ->

            val setLayoutBindings = VkDescriptorSetLayoutBinding.calloc(4, stack)
            val shaderStorageBinding = setLayoutBindings[0]
            shaderStorageBinding.binding(0)
            shaderStorageBinding.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER)
            shaderStorageBinding.descriptorCount(1)
            shaderStorageBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT or VK_SHADER_STAGE_FRAGMENT_BIT)
            val textureSamplerBinding = setLayoutBindings[1]
            textureSamplerBinding.binding(1)
            textureSamplerBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER)
            textureSamplerBinding.descriptorCount(2)
            textureSamplerBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)
            val texturesBinding = setLayoutBindings[2]
            texturesBinding.binding(2)
            texturesBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE)
            texturesBinding.descriptorCount(instance.maxNumDescriptorImages)
            texturesBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)
            val textTextureBinding = setLayoutBindings[3]
            textTextureBinding.binding(3)
            textTextureBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE)
            textTextureBinding.descriptorCount(1)
            textTextureBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)
            this.vkDescriptorSetLayout = instance.boiler.descriptors.createLayout(
                stack, setLayoutBindings, "GraviksBase"
            )
            this.vkPipelineLayout = instance.boiler.pipelines.createLayout(
                stack, null, "GraviksBase", vkDescriptorSetLayout
            )

            val renderPass = createGraviksRenderPass(
                instance.boiler.vkDevice(), TARGET_COLOR_FORMAT, stack
            )
            this.vkRenderPass = renderPass

            val vertexShader = instance.boiler.pipelines.createShaderModule(
                stack, "graviks2d/shaders/basic.vert.spv", "GraviksBasicVertexShader"
            )
            val fragmentShader = instance.boiler.pipelines.createShaderModule(
                stack, "graviks2d/shaders/basic.frag.spv", "GraviksBasicFragmentShader"
            )

            val builder = GraphicsPipelineBuilder(instance.boiler, stack)
            builder.shaderStages(
                ShaderInfo(VK_SHADER_STAGE_VERTEX_BIT, vertexShader, null),
                ShaderInfo(VK_SHADER_STAGE_FRAGMENT_BIT, fragmentShader, null)
            )
            builder.ciPipeline.pVertexInputState(createGraviksPipelineVertexInput(stack))
            builder.simpleInputAssembly()
            builder.dynamicViewports(1)
            builder.dynamicStates(
                VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR
            )
            builder.simpleRasterization(VK_CULL_MODE_NONE)
            builder.noMultisampling()
            builder.simpleColorBlending(1)
            builder.ciPipeline.layout(vkPipelineLayout)
            builder.ciPipeline.renderPass(vkRenderPass)
            builder.ciPipeline.subpass(0)

            this.vkPipeline = builder.build("GraviksCore")

            vkDestroyShaderModule(instance.boiler.vkDevice(), vertexShader, null)
            vkDestroyShaderModule(instance.boiler.vkDevice(), fragmentShader, null)
        }
    }

    fun destroy() {
        vkDestroyPipeline(this.instance.boiler.vkDevice(), this.vkPipeline, null)
        vkDestroyPipelineLayout(this.instance.boiler.vkDevice(), this.vkPipelineLayout, null)
        vkDestroyDescriptorSetLayout(this.instance.boiler.vkDevice(), this.vkDescriptorSetLayout, null)
        vkDestroyRenderPass(this.instance.boiler.vkDevice(), this.vkRenderPass, null)
    }
}
