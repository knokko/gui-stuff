package dsl.pm2.renderer

import com.github.knokko.boiler.buffer.VmaBuffer
import com.github.knokko.boiler.commands.CommandRecorder
import com.github.knokko.boiler.exceptions.VulkanFailureException.assertVkSuccess
import com.github.knokko.boiler.images.VmaImage
import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.boiler.sync.ResourceUsage
import dsl.pm2.interpreter.Pm2RuntimeError
import dsl.pm2.interpreter.Pm2Type
import dsl.pm2.interpreter.program.Pm2DynamicParameterProcessor
import dsl.pm2.interpreter.program.Pm2MatrixProcessor
import dsl.pm2.interpreter.value.Pm2Value
import org.joml.Matrix3x2f
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.util.vma.Vma.*
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.VK10.*
import kotlin.jvm.Throws

/**
 * The size in bytes of a 3x2 float matrix. Due to alignment rules, this is 48 bytes, which is twice as large as
 * I was hoping.
 */
internal const val MATRIX_SIZE = 48

class Pm2Scene internal constructor(
    private val boiler: BoilerInstance,
    vkDescriptorSetLayout: Long,
    private val backgroundRed: Int,
    private val backgroundGreen: Int,
    private val backgroundBlue: Int,
    private val width: Int,
    private val height: Int,
) {
    private val colorImage: VmaImage
    private val framebuffer: Long

    private val renderPass: Long
    private val commandPool: Long
    private val commandBuffer: VkCommandBuffer
    private val fence: Long

    private val descriptorPool: Long
    private val descriptorSet: Long
    private var matrixBuffer: VmaBuffer? = null
    private var matrixBufferCount = 0

    init {

        val imageFormat = VK_FORMAT_R8G8B8A8_SRGB
        stackPush().use { stack ->
            this.colorImage = boiler.images.createSimple(
                stack, width, height, imageFormat,
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT or VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                VK_IMAGE_ASPECT_COLOR_BIT, "Pm2SceneColorImage"
            )

            val attachments = VkAttachmentDescription.calloc(1, stack)
            val colorAttachment = attachments[0]
            colorAttachment.flags(0)
            colorAttachment.format(imageFormat)
            colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT)
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)

            val subpassColorAttachments = VkAttachmentReference.calloc(1, stack)
            val subpassColorAttachment = subpassColorAttachments[0]
            subpassColorAttachment.attachment(0)
            subpassColorAttachment.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)

            val subpasses = VkSubpassDescription.calloc(1, stack)
            val subpass = subpasses[0]
            subpass.flags(0)
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
            subpass.pInputAttachments(null)
            subpass.colorAttachmentCount(1)
            subpass.pColorAttachments(subpassColorAttachments)
            subpass.pResolveAttachments(null)
            subpass.pDepthStencilAttachment(null)
            subpass.pPreserveAttachments(null)

            val dependencies = VkSubpassDependency.calloc(1, stack)
            dependencies[0].srcSubpass(0)
            dependencies[0].dstSubpass(VK_SUBPASS_EXTERNAL)
            dependencies[0].srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            dependencies[0].dstStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT)
            dependencies[0].srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
            dependencies[0].dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT)
            dependencies[0].dependencyFlags(0)

            val ciRenderPass = VkRenderPassCreateInfo.calloc(stack)
            ciRenderPass.`sType$Default`()
            ciRenderPass.flags(0)
            ciRenderPass.pAttachments(attachments)
            ciRenderPass.pSubpasses(subpasses)
            ciRenderPass.pDependencies(dependencies)

            val pRenderPass = stack.callocLong(1)
            assertVkSuccess(vkCreateRenderPass(
                boiler.vkDevice(), ciRenderPass, null, pRenderPass
            ), "CreateRenderPass", "Pm2RenderPass")
            renderPass = pRenderPass[0]
            boiler.debug.name(stack, renderPass, VK_OBJECT_TYPE_RENDER_PASS, "Pm2RenderPass")

            framebuffer = boiler.images.createFramebuffer(
                stack, renderPass, width, height, "Pm2Framebuffer", colorImage.vkImageView
            )
            commandPool = boiler.commands.createPool(
                0, boiler.queueFamilies().graphics.index, "Pm2DrawSceneCommandPool"
            )
            commandBuffer = boiler.commands.createPrimaryBuffers(commandPool, 1, "Pm2DrawSceneCommandBuffer")[0]
            fence = boiler.sync.createFences(true, 1, "Pm2DrawSceneFence")[0]

            val descriptorPoolSizes = VkDescriptorPoolSize.calloc(1, stack)
            descriptorPoolSizes.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
            descriptorPoolSizes.descriptorCount(1)

            val ciDescriptorPool = VkDescriptorPoolCreateInfo.calloc(stack)
            ciDescriptorPool.`sType$Default`()
            ciDescriptorPool.flags(0)
            ciDescriptorPool.maxSets(1)
            ciDescriptorPool.pPoolSizes(descriptorPoolSizes)

            val pDescriptorPool = stack.callocLong(1)
            assertVkSuccess(vkCreateDescriptorPool(
                    boiler.vkDevice(), ciDescriptorPool, null, pDescriptorPool
            ), "CreateDescriptorPool", "Pm2DrawSceneDescriptorPool")
            descriptorPool = pDescriptorPool[0]
            descriptorSet = boiler.descriptors.allocate(
                stack, 1, descriptorPool, "Pm2DrawSceneDescriptorSet", vkDescriptorSetLayout
            )[0]
        }
    }

    private fun ensureMatrixBuffer(numMatrices: Int) {
        if (numMatrices > matrixBufferCount) {
            if (matrixBuffer != null) {
                vmaDestroyBuffer(boiler.vmaAllocator(), matrixBuffer!!.vkBuffer, matrixBuffer!!.vmaAllocation)
            }

            val matrixBufferSize = MATRIX_SIZE.toLong() * numMatrices
            // Not all hardware supports uniform buffers larger than 16KB
            if (matrixBufferSize > 16_000L) throw Pm2RuntimeError("Too many dynamic matrices: $numMatrices")
            // TODO Optionally use storage buffers instead

            matrixBuffer = boiler.buffers.create(
                matrixBufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT or VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                "Pm2MatrixDeviceBuffer"
            )
            matrixBufferCount = numMatrices

            stackPush().use { stack ->
                val descriptorWrites = VkWriteDescriptorSet.calloc(1, stack)
                descriptorWrites.`sType$Default`()
                descriptorWrites.dstSet(descriptorSet)
                descriptorWrites.dstBinding(0)
                descriptorWrites.dstArrayElement(0)
                descriptorWrites.descriptorCount(1)
                descriptorWrites.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                descriptorWrites.pBufferInfo(boiler.descriptors.bufferInfo(stack, matrixBuffer))

                vkUpdateDescriptorSets(boiler.vkDevice(), descriptorWrites, null)
            }
        }
    }

    fun awaitLastDraw() {
        stackPush().use { stack ->
            assertVkSuccess(vkWaitForFences(
                boiler.vkDevice(), stack.longs(fence), true, 10_000_000_000L
            ), "WaitForFences", "Pm2Scene.awaitLastDraw")
        }
    }

    @Throws(Pm2RuntimeError::class)
    fun drawAndCopy(
            instance: Pm2Instance, meshes: List<Pair<Pm2Mesh, Map<String, Pm2Value>>>, cameraMatrix: Matrix3x2f, signalSemaphore: Long?,
            destImage: Long, oldLayout: Int, srcAccessMask: Int, srcStageMask: Int,
            newLayout: Int, dstAccessMask: Int, dstStageMask: Int,
            offsetX: Int, offsetY: Int, blitSizeX: Int, blitSizeY: Int
    ) {
        val requiredNumMatrices = meshes.sumOf { it.first.matrices.size }
        ensureMatrixBuffer(requiredNumMatrices)

        val meshesWithMatrices = meshes.map { (mesh, dynamicParameterValues) ->
            val matrices = ArrayList<Triple<Matrix3x2f, Map<String, Pm2Value>, Map<String, Pm2Type>>>(mesh.matrices.size)
            for (matrix in mesh.matrices) {
                if (matrix != null) {
                    val (parentMatrix, dynamicParentParameterValues, dynamicParentParameterTypes) = matrices[matrix.parentIndex]

                    val childParameterValues = if (matrix.dynamicMatrixPropagationInstructions != null) {
                        val parameterProcessor = Pm2DynamicParameterProcessor(
                                matrix.dynamicMatrixPropagationInstructions,
                                dynamicParentParameterValues,
                                dynamicParentParameterTypes
                        )
                        parameterProcessor.execute()
                        parameterProcessor.result
                    } else {
                        if (matrix.parentIndex != 0) throw Pm2RuntimeError("Parent index must be 0 when propagation instructions are null")
                        dynamicParentParameterValues
                    }
                    val relativeMatrix = Pm2MatrixProcessor(
                            matrix, matrix.dynamicParameterTypes, childParameterValues
                    ).execute()
                    matrices.add(Triple(parentMatrix.mul(relativeMatrix, relativeMatrix), childParameterValues, matrix.dynamicParameterTypes))
                } else matrices.add(Triple(Matrix3x2f(), dynamicParameterValues, mesh.dynamicParameterTypes))
            }
            Pair(mesh, matrices)
        }

        val pipelineInfo = Pm2PipelineInfo(renderPass, 0) { stack, blendState ->
            val blendAttachments = VkPipelineColorBlendAttachmentState.calloc(1, stack)
            val blendAttachment = blendAttachments[0]
            blendAttachment.blendEnable(false)
            blendAttachment.colorWriteMask(
                VK_COLOR_COMPONENT_R_BIT or VK_COLOR_COMPONENT_G_BIT or VK_COLOR_COMPONENT_B_BIT or VK_COLOR_COMPONENT_A_BIT
            )

            blendState.`sType$Default`()
            blendState.flags(0)
            blendState.logicOpEnable(false)
            blendState.attachmentCount(1)
            blendState.pAttachments(blendAttachments)
        }

        stackPush().use { stack ->
            boiler.sync.waitAndReset(stack, fence, 10_000_000_000L)
            assertVkSuccess(vkResetCommandPool(
                boiler.vkDevice(), commandPool, 0
            ), "ResetCommandPool", "Pm2SceneDrawCommandPool")

            val recorder = CommandRecorder.begin(commandBuffer, boiler, stack, "Pm2SceneDrawCommands")

            var matrixIndex = 0
            val meshesWithMatrixIndices = mutableListOf<Pair<Pm2Mesh, Int>>()
            val hostMatrixBuffer = stack.calloc(MATRIX_SIZE * requiredNumMatrices).asFloatBuffer()
            for ((mesh, matrices) in meshesWithMatrices) {
                meshesWithMatrixIndices.add(Pair(mesh, matrixIndex))
                for ((matrix, _, _) in matrices) {

                    // Yeah... alignment rules...
                    val bufferIndex = MATRIX_SIZE * matrixIndex / Float.SIZE_BYTES
                    hostMatrixBuffer.put(bufferIndex, matrix.m00)
                    hostMatrixBuffer.put(bufferIndex + 1, matrix.m01)
                    hostMatrixBuffer.put(bufferIndex + 4, matrix.m10)
                    hostMatrixBuffer.put(bufferIndex + 5, matrix.m11)
                    hostMatrixBuffer.put(bufferIndex + 8, matrix.m20)
                    hostMatrixBuffer.put(bufferIndex + 9, matrix.m21)
                    matrixIndex += 1
                }
            }
            vkCmdUpdateBuffer(commandBuffer, matrixBuffer!!.vkBuffer, 0, hostMatrixBuffer)

            recorder.bufferBarrier(
                matrixBuffer!!.vkBuffer, 0, matrixBuffer!!.size,
                ResourceUsage(VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT),
                ResourceUsage(VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_VERTEX_SHADER_BIT)
            )

            val clearValues = VkClearValue.calloc(1, stack)
            clearValues.color().float32(0, backgroundRed / 255f)
            clearValues.color().float32(1, backgroundGreen / 255f)
            clearValues.color().float32(2, backgroundBlue / 255f)
            clearValues.color().float32(3, 1f)

            val biRenderPass = VkRenderPassBeginInfo.calloc(stack)
            biRenderPass.`sType$Default`()
            biRenderPass.renderPass(renderPass)
            biRenderPass.framebuffer(framebuffer)
            biRenderPass.renderArea().offset().set(0, 0)
            biRenderPass.renderArea().extent().set(width, height)
            biRenderPass.clearValueCount(1)
            biRenderPass.pClearValues(clearValues)

            vkCmdBeginRenderPass(commandBuffer, biRenderPass, VK_SUBPASS_CONTENTS_INLINE)
            recorder.dynamicViewportAndScissor(width, height)

            instance.recordDraw(commandBuffer, pipelineInfo, descriptorSet, meshesWithMatrixIndices, cameraMatrix)

            vkCmdEndRenderPass(commandBuffer)

            if (oldLayout != VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                recorder.transitionColorLayout(
                    destImage, oldLayout, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    ResourceUsage(srcAccessMask, srcStageMask),
                    ResourceUsage(VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
                )
            }

            val blitRegion = VkImageBlit.calloc(1, stack)
            boiler.images.subresourceLayers(stack, blitRegion.srcSubresource(), VK_IMAGE_ASPECT_COLOR_BIT)
            blitRegion.srcOffsets(0).set(0, 0, 0)
            blitRegion.srcOffsets(1).set(width, height, 1)
            boiler.images.subresourceLayers(stack, blitRegion.dstSubresource(), VK_IMAGE_ASPECT_COLOR_BIT)
            blitRegion.dstOffsets(0).set(offsetX, offsetY, 0)
            blitRegion.dstOffsets(1).set(blitSizeX, blitSizeY, 1)

            vkCmdBlitImage(
                commandBuffer, colorImage.vkImage, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                destImage, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blitRegion, VK_FILTER_LINEAR
            )

            if (newLayout != VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                recorder.transitionColorLayout(
                    destImage, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, newLayout,
                    ResourceUsage(VK_ACCESS_TRANSFER_WRITE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT),
                    ResourceUsage(dstAccessMask, dstStageMask)
                )
            }

            assertVkSuccess(vkEndCommandBuffer(commandBuffer), "EndCommandBuffer", "Pm2SceneDrawAndCopy")

            val signalSemaphores = if (signalSemaphore != null) longArrayOf(signalSemaphore) else LongArray(0)
            instance.boiler.queueFamilies().graphics.queues.random().submit(
                commandBuffer, "Pm2Scene", emptyArray(), fence, *signalSemaphores
            )
        }
    }

    fun destroy() {
        stackPush().use { stack ->
            assertVkSuccess(vkWaitForFences(
                boiler.vkDevice(), stack.longs(fence), true, 10_000_000_000L
            ), "WaitForFences", "Pm2SceneDestroy")
        }
        vkDestroyFence(boiler.vkDevice(), fence, null)
        vkDestroyCommandPool(boiler.vkDevice(), commandPool, null)
        vkDestroyRenderPass(boiler.vkDevice(), renderPass, null)
        vkDestroyFramebuffer(boiler.vkDevice(), framebuffer, null)
        vkDestroyImageView(boiler.vkDevice(), colorImage.vkImageView, null)
        vmaDestroyImage(boiler.vmaAllocator(), colorImage.vkImage, colorImage.vmaAllocation)
        if (matrixBufferCount > 0) {
            vmaDestroyBuffer(boiler.vmaAllocator(), matrixBuffer!!.vkBuffer, matrixBuffer!!.vmaAllocation)
        }
        vkDestroyDescriptorPool(boiler.vkDevice(), descriptorPool, null)
    }
}
