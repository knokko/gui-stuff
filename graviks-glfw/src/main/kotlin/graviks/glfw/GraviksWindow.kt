package graviks.glfw

import com.github.knokko.boiler.builder.BoilerBuilder
import com.github.knokko.boiler.builder.BoilerBuilder.DEFAULT_VK_DEVICE_CREATOR
import com.github.knokko.boiler.builder.BoilerSwapchainBuilder
import com.github.knokko.boiler.builder.swapchain.SimpleSurfaceFormatPicker
import com.github.knokko.boiler.exceptions.VulkanFailureException.assertVkSuccess
import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.boiler.sync.ResourceUsage
import graviks2d.context.GraviksContext
import graviks2d.core.GraviksInstance
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.*
import org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME
import org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.vkGetPhysicalDeviceFeatures2KHR
import org.lwjgl.vulkan.KHRIncrementalPresent.VK_KHR_INCREMENTAL_PRESENT_EXTENSION_NAME
import org.lwjgl.vulkan.KHRPresentId.VK_KHR_PRESENT_ID_EXTENSION_NAME
import org.lwjgl.vulkan.KHRPresentWait.VK_KHR_PRESENT_WAIT_EXTENSION_NAME
import org.lwjgl.vulkan.KHRPresentWait.vkWaitForPresentKHR
import org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR
import org.lwjgl.vulkan.KHRSwapchain.*
import org.lwjgl.vulkan.VK10.*
import java.lang.System.nanoTime

class GraviksWindow(
    initialWidth: Int,
    initialHeight: Int,
    builder: BoilerBuilder,
    private val createContext: (instance: GraviksInstance, width: Int, height: Int) -> GraviksContext
) {

    val boiler: BoilerInstance
    val graviksInstance: GraviksInstance

    val canAwaitPresent: Boolean
    private var lastPresentId = 0
    val hasIncrementalPresent: Boolean

    var currentGraviksContext: GraviksContext? = null

    init {
        var canAwaitPresent = false

        this.boiler = builder
            .window(0L, initialWidth, initialHeight, BoilerSwapchainBuilder(
                VK_IMAGE_USAGE_TRANSFER_DST_BIT
            ).surfaceFormatPicker(SimpleSurfaceFormatPicker(VK_FORMAT_B8G8R8A8_UNORM)))
            .desiredVkInstanceExtensions(listOf(VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME))
            .desiredVkDeviceExtensions(listOf(
                VK_KHR_INCREMENTAL_PRESENT_EXTENSION_NAME,
                VK_KHR_PRESENT_WAIT_EXTENSION_NAME,
                VK_KHR_PRESENT_ID_EXTENSION_NAME
            ))
            .vkDeviceCreator { stack, vkPhysicalDevice, deviceExtensions, ciDevice ->
                if (deviceExtensions.contains(VK_KHR_PRESENT_WAIT_EXTENSION_NAME)) {
                    val presentWaitSupport = VkPhysicalDevicePresentWaitFeaturesKHR.calloc(stack)
                    presentWaitSupport.`sType$Default`()

                    val presentIdSupport = VkPhysicalDevicePresentIdFeaturesKHR.calloc(stack)
                    presentIdSupport.`sType$Default`()
                    presentIdSupport.pNext(presentWaitSupport.address())

                    val supportedFeatures2 = VkPhysicalDeviceFeatures2.calloc(stack)
                    supportedFeatures2.`sType$Default`()
                    supportedFeatures2.pNext(presentIdSupport.address())

                    vkGetPhysicalDeviceFeatures2KHR(
                        vkPhysicalDevice,
                        supportedFeatures2
                    )

                    if (presentWaitSupport.presentWait() && presentIdSupport.presentId()) {
                        ciDevice.pNext(presentIdSupport)
                        ciDevice.pNext(presentWaitSupport)
                        canAwaitPresent = true
                    }
                }
                DEFAULT_VK_DEVICE_CREATOR.vkCreateDevice(stack, vkPhysicalDevice, deviceExtensions, ciDevice)
            }
        .build()

        this.canAwaitPresent = canAwaitPresent
        this.hasIncrementalPresent = boiler.deviceExtensions.contains(VK_KHR_INCREMENTAL_PRESENT_EXTENSION_NAME)

        graviksInstance = GraviksInstance(boiler)
    }

    fun presentFrame(waitUntilVisible: Boolean, fillPresentRegions: ((MemoryStack) -> VkRectLayerKHR.Buffer)?) {
        drawAndPresent(waitUntilVisible, null, fillPresentRegions)
    }

    fun drawAndPresent(
        waitUntilVisible: Boolean,
        drawFunction: ((GraviksContext) -> Unit)?,
        fillPresentRegions: ((MemoryStack) -> VkRectLayerKHR.Buffer)?
    ) {
        if (waitUntilVisible && !canAwaitPresent) {
            throw UnsupportedOperationException("Waiting until presentation is not supported by the Vulkan implementation")
        }

        // The swapchain image will be null when the window is minified. In this case, we should not do anything
        val swapchainImage = boiler.swapchains.acquireNextImage(VK_PRESENT_MODE_FIFO_KHR) ?: return

        if (currentGraviksContext == null || currentGraviksContext!!.width != swapchainImage.width || currentGraviksContext!!.height != swapchainImage.height) {
            if (currentGraviksContext != null) {
                // Awaiting completion ensures that all wait semaphores of the context are awaited, which is needed
                // to avoid double semaphore signal operations 'downstream'
                currentGraviksContext!!.awaitCompletion()
                currentGraviksContext!!.destroy()
            }
            currentGraviksContext = createContext(graviksInstance, swapchainImage.width, swapchainImage.height)
        }
        val graviksContext = currentGraviksContext!!

        if (drawFunction != null) drawFunction(graviksContext)

        stackPush().use { stack ->

            graviksContext.addWaitSemaphore(swapchainImage.acquireSemaphore, VK_PIPELINE_STAGE_TRANSFER_BIT)
            graviksContext.copyColorImageTo(
                destImage = swapchainImage.vkImage, destImageFormat = boiler.swapchainSettings.surfaceFormat.format,
                destBuffer = null, signalSemaphore = swapchainImage.presentSemaphore,
                originalImageLayout = VK_IMAGE_LAYOUT_UNDEFINED, finalImageLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                imageSrcUsage = ResourceUsage(0, VK_PIPELINE_STAGE_TRANSFER_BIT),
                // There is no need to give proper destinations masks since vkQueuePresentKHR takes care of that
                imageDstUsage = null, shouldAwaitCompletion = false
            )

            val incrementalPresentAddress = if (hasIncrementalPresent && fillPresentRegions != null) {
                val presentRectangles = fillPresentRegions(stack)

                val presentRegions = VkPresentRegionKHR.calloc(1, stack)
                presentRegions.rectangleCount(presentRectangles.capacity())
                presentRegions.pRectangles(presentRectangles)

                val incrementalPresent = VkPresentRegionsKHR.calloc(stack)
                incrementalPresent.`sType$Default`()
                incrementalPresent.swapchainCount(1)
                incrementalPresent.pRegions(presentRegions)

                incrementalPresent.address()
            } else 0L

            val pPresentId = stack.longs(lastPresentId + 1L)

            val presentIdAddress = if (canAwaitPresent) {
                val presentIdInfo = VkPresentIdKHR.calloc(stack)
                presentIdInfo.`sType$Default`()
                presentIdInfo.pNext(incrementalPresentAddress)
                presentIdInfo.swapchainCount(1)
                presentIdInfo.pPresentIds(pPresentId)

                presentIdInfo.address()
            } else 0L

            boiler.swapchains.presentImage(swapchainImage) { presentInfo ->
                if (waitUntilVisible) presentInfo.pNext(presentIdAddress)
                else presentInfo.pNext(incrementalPresentAddress)
            }

            if (waitUntilVisible) {
                val startTime = nanoTime()
                assertVkSuccess(vkWaitForPresentKHR(
                    boiler.vkDevice(), swapchainImage.vkSwapchain, pPresentId[0], 1_000_000_000L
                ), "WaitForPresentKHR", "GraviksWindow")
                println("presentation took ${(nanoTime() - startTime) / 1000} microseconds")
                lastPresentId += 1
            }
        }
    }

    fun destroy() {
        if (currentGraviksContext != null) currentGraviksContext!!.destroy()
        graviksInstance.destroy()
        boiler.destroyInitialObjects()
    }
}
