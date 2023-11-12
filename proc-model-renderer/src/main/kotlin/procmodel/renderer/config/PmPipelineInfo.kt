package procmodel.renderer.config

import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout
import org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkDevice
import java.nio.IntBuffer

class PmPipelineInfo<Matrix>(
    val pipelineLayout: Long,
    val descriptorSetLayout: Long,
    val createDescriptorPool: (amount: Int) -> Long,
    val createGraphicsPipeline: (PmRenderPassInfo) -> Long,

    val pushCameraMatrix: (Matrix, MemoryStack, VkCommandBuffer) -> Unit,
    val pushMatrixIndexOffset: (IntBuffer, VkCommandBuffer) -> Unit,
) {
    fun destroy(vkDevice: VkDevice) {
        vkDestroyPipelineLayout(vkDevice, pipelineLayout, null)
        vkDestroyDescriptorSetLayout(vkDevice, descriptorSetLayout, null)
    }
}
