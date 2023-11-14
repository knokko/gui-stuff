package graviks2d.resource.text

import com.github.knokko.boiler.commands.CommandRecorder
import com.github.knokko.boiler.sync.ResourceUsage
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*

internal fun rasterizeTextAtlas(
    recorder: CommandRecorder, commandBuffer: VkCommandBuffer, textCache: TextShapeCache, isFirstDraw: Boolean
) {
    stackPush().use { stack ->

        if (textCache.currentVertexIndex > 0 || isFirstDraw) {
            val clearValues = VkClearValue.calloc(1, stack)
            val clearValue = clearValues[0]
            clearValue.color().int32(0, 0)

            val biRenderPass = VkRenderPassBeginInfo.calloc(stack)
            biRenderPass.`sType$Default`()
            biRenderPass.renderPass(textCache.context.instance.textPipelines.vkRenderPass)
            biRenderPass.framebuffer(textCache.textAtlasFramebuffer)
            biRenderPass.renderArea {
                it.offset().set(0, 0)
                it.extent().set(textCache.width, textCache.height)
            }
            biRenderPass.clearValueCount(1)
            biRenderPass.pClearValues(clearValues)

            if (textCache.currentVertexIndex > 0) {
                vkCmdBeginRenderPass(commandBuffer, biRenderPass, VK_SUBPASS_CONTENTS_INLINE)
                recorder.dynamicViewportAndScissor(textCache.width, textCache.height)
                vkCmdBindPipeline(
                        commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                        textCache.context.instance.textPipelines.countPipeline
                )
                vkCmdBindVertexBuffers(commandBuffer, 0, stack.longs(textCache.countVertexBuffer.vkBuffer), stack.longs(0))
                vkCmdDraw(commandBuffer, textCache.currentVertexIndex, 1, 0, 0)
                vkCmdNextSubpass(commandBuffer, VK_SUBPASS_CONTENTS_INLINE)
                vkCmdBindPipeline(
                        commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                        textCache.context.instance.textPipelines.oddPipeline
                )
                vkCmdBindDescriptorSets(
                        commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                        textCache.context.instance.textPipelines.oddPipelineLayout,
                        0, stack.longs(textCache.descriptorSet), null
                )
                vkCmdBindVertexBuffers(commandBuffer, 0, stack.longs(textCache.oddVertexBuffer.vkBuffer), stack.longs(0))
                vkCmdDraw(commandBuffer, 6, 1, 0, 0)
                vkCmdEndRenderPass(commandBuffer)
            }

            var oldLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
            var oldUsage: ResourceUsage? = ResourceUsage(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)

            if (isFirstDraw && textCache.currentVertexIndex == 0) {
                oldLayout = VK_IMAGE_LAYOUT_UNDEFINED
                oldUsage = null
            }

            recorder.transitionColorLayout(
                    textCache.textOddAtlas.vkImage, oldLayout,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    oldUsage, ResourceUsage(VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
            )
        }
    }
}
