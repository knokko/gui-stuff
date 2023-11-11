package procmodel.renderer.config

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.VK_NULL_HANDLE
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo
import org.lwjgl.vulkan.VkPipelineRenderingCreateInfo

/**
 * ProcModel needs to create graphics pipelines, and these need to be compatible with the renderpass (or dynamic
 * rendering) of the application, and its blend state.
 *
 * To make this possible, the application needs to provide this information using a `PmRenderPassInfo`.
 * Applications will need 1 instance of `PmRenderPassInfo` per (renderpass, blendState) combination.
 */
class PmRenderPassInfo(
    val populateRenderPass: (VkGraphicsPipelineCreateInfo, MemoryStack) -> Unit,
    val populateBlendState: (VkGraphicsPipelineCreateInfo, MemoryStack) -> Unit
) {
    companion object {
        fun withRenderPass(
            renderPass: Long, subpass: Int,
            populateBlendState: (VkGraphicsPipelineCreateInfo, MemoryStack) -> Unit
        ) = PmRenderPassInfo({ ciPipeline, _ ->
            ciPipeline.renderPass(renderPass)
            ciPipeline.subpass(subpass)
        }, populateBlendState)

        fun dynamicRendering(
            populateRendering: (VkPipelineRenderingCreateInfo, MemoryStack) -> Unit,
            populateBlendState: (VkGraphicsPipelineCreateInfo, MemoryStack) -> Unit
        ) = PmRenderPassInfo({ ciPipeline, stack ->
            val renderingInfo = VkPipelineRenderingCreateInfo.calloc(stack)
            renderingInfo.`sType$Default`()
            populateRendering(renderingInfo, stack)

            ciPipeline.renderPass(VK_NULL_HANDLE)
            ciPipeline.pNext(renderingInfo)
        }, populateBlendState)
    }
}
