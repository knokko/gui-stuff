package procmodel.playground

import com.github.knokko.boiler.builder.BoilerBuilder
import com.github.knokko.boiler.builder.BoilerSwapchainBuilder
import com.github.knokko.boiler.builder.instance.ValidationFeatures
import com.github.knokko.boiler.commands.CommandRecorder
import com.github.knokko.boiler.exceptions.VulkanFailureException.assertVkSuccess
import com.github.knokko.boiler.pipelines.GraphicsPipelineBuilder
import com.github.knokko.boiler.swapchain.SwapchainResourceManager
import com.github.knokko.boiler.sync.ResourceUsage
import com.github.knokko.boiler.sync.WaitSemaphore
import graviks2d.util.Color
import org.joml.Matrix3x2f
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR
import org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkDevice
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features
import org.lwjgl.vulkan.VkRenderingAttachmentInfo
import org.lwjgl.vulkan.VkRenderingInfo
import procmodel.model.PmModel
import procmodel.renderer.MeshDrawTask
import procmodel.renderer.PmInstance
import procmodel.renderer.config.PmRenderPassInfo
import procmodel2.Pm2dVertex
import procmodel2.createPipelineInfo2d
import java.lang.Thread.sleep

fun main() {
    val boiler = BoilerBuilder(
        VK_API_VERSION_1_3, "Pm2dPlayground", VK_MAKE_VERSION(0, 1, 0)
    )
        .validation(ValidationFeatures(true, true, false, true, true))
        .vkDeviceCreator { stack, vkPhysicalDevice, strings, vkDeviceCreateInfo ->
            // TODO Fix this in vk-boiler
            val pDevice = stack.callocPointer(1)
            val features = VkPhysicalDeviceVulkan13Features.calloc(stack)
            features.`sType$Default`()
            features.dynamicRendering(true)
            vkDeviceCreateInfo.pNext(features)
            assertVkSuccess(vkCreateDevice(
                vkPhysicalDevice, vkDeviceCreateInfo, null, pDevice
            ), "CreateDevice", null)
            VkDevice(pDevice[0], vkPhysicalDevice, vkDeviceCreateInfo)
        }
        .window(0, 1200, 700, BoilerSwapchainBuilder(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT))
        .build()

    val pipelineInfo = createPipelineInfo2d(boiler)
    val procModel = PmInstance(boiler, pipelineInfo)
    val cameraMatrix = Matrix3x2f()
    val renderPassInfo = PmRenderPassInfo.dynamicRendering(
        { ciRendering, stack ->
            ciRendering.colorAttachmentCount(1)
            ciRendering.pColorAttachmentFormats(stack.ints(boiler.swapchainSettings.surfaceFormat.format))
        },
        { ciPipeline, stack ->
            GraphicsPipelineBuilder(ciPipeline, boiler, stack).simpleColorBlending(1)
        }
    )

    val model = PmModel(
        vertices = listOf(
            Pm2dVertex(-0.5f, -0.5f, Color.RED, 0),
            Pm2dVertex(0.5f, -0.5f, Color.RED, 0),
            Pm2dVertex(0f, 0.5f, Color.RED, 0),
        ),
        matrices = listOf(null),
        dynamicParameters = emptyMap()
    )

    val mesh = procModel.meshes.allocate(model)

    val commandPool = boiler.commands.createPool(0, boiler.queueFamilies().graphics.index, "DrawPool")
    val commandBuffer = boiler.commands.createPrimaryBuffers(commandPool, 1, "DrawBuffer")[0]
    val commandFence = boiler.sync.createFences(true, 1, "CommandFence")[0]

    val swapchainResources = SwapchainResourceManager(
        { swapchainImage -> stackPush().use { stack ->
            boiler.images.createSimpleView(
                stack, swapchainImage.vkImage,
                boiler.swapchainSettings.surfaceFormat.format,
                VK_IMAGE_ASPECT_COLOR_BIT, "SwapchainImageView"
            )
        } },
        { imageView -> vkDestroyImageView(boiler.vkDevice(), imageView, null) }
    )

    while (!glfwWindowShouldClose(boiler.glfwWindow())) {
        glfwPollEvents()

        val swapchainImage = boiler.swapchains.acquireNextImage(VK_PRESENT_MODE_FIFO_KHR)
        if (swapchainImage == null) {
            sleep(100)
            continue
        }

        stackPush().use { stack ->
            boiler.sync.waitAndReset(stack, commandFence, 5_000_000_000L)
            assertVkSuccess(vkResetCommandPool(
                boiler.vkDevice(), commandPool, 0
            ), "ResetCommandPool", "Drawing")

            val commands = CommandRecorder.begin(commandBuffer, boiler, stack, "Drawing")

            commands.transitionColorLayout(
                swapchainImage.vkImage, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, null,
                ResourceUsage(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            )

            val colorAttachments = VkRenderingAttachmentInfo.calloc(1, stack)
            colorAttachments.`sType$Default`()
            colorAttachments.imageView(swapchainResources[swapchainImage])
            colorAttachments.imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
            colorAttachments.resolveMode(VK_RESOLVE_MODE_NONE)
            colorAttachments.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
            colorAttachments.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
            colorAttachments.clearValue().color().float32().put(0.1f).put(0.3f).put(0.7f)

            val renderInfo = VkRenderingInfo.calloc(stack)
            renderInfo.`sType$Default`()
            renderInfo.renderArea().offset().set(0, 0)
            renderInfo.renderArea().extent().set(swapchainImage.width, swapchainImage.height)
            renderInfo.layerCount(1)
            renderInfo.pColorAttachments(colorAttachments)

            procModel.transformationMatrices.computeAndStore(listOf(MeshDrawTask(mesh, emptyMap())))

            vkCmdBeginRendering(commandBuffer, renderInfo)

            procModel.commands.recordDrawingCommands(
                commandBuffer, renderPassInfo,
                procModel.transformationMatrices.descriptorSet,
                swapchainImage.width, swapchainImage.height,
                listOf(Pair(mesh, 0)), cameraMatrix
            )
            vkCmdEndRendering(commandBuffer)

            commands.transitionColorLayout(
                swapchainImage.vkImage, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                ResourceUsage(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
                ResourceUsage(0, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
            )

            commands.end()

            boiler.queueFamilies().graphics.queues.first().submit(
                commandBuffer, "Drawing",
                arrayOf(WaitSemaphore(swapchainImage.acquireSemaphore, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)),
                commandFence, swapchainImage.presentSemaphore
            )
        }

        boiler.swapchains.presentImage(swapchainImage)
    }

    vkDeviceWaitIdle(boiler.vkDevice())
    procModel.meshes.destroy(mesh)
    procModel.destroy()
    vkDestroyFence(boiler.vkDevice(), commandFence, null)
    vkDestroyCommandPool(boiler.vkDevice(), commandPool, null)
    boiler.destroyInitialObjects()
}
