package procmodel.editor2d

import com.github.knokko.boiler.commands.CommandRecorder
import com.github.knokko.boiler.exceptions.VulkanFailureException.assertVkSuccess
import com.github.knokko.boiler.images.VmaImage
import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.boiler.pipelines.GraphicsPipelineBuilder
import com.github.knokko.boiler.sync.ResourceUsage
import graviks2d.target.GraviksTarget
import graviks2d.util.Color
import gruviks.component.Component
import gruviks.component.RectangularDrawnRegion
import gruviks.component.RenderResult
import gruviks.event.*
import gruviks.feedback.RenderFeedback
import gruviks.feedback.RequestKeyboardFocusFeedback
import org.joml.Matrix3x2f
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.util.vma.Vma.vmaDestroyImage
import org.lwjgl.vulkan.VK13.*
import org.lwjgl.vulkan.VkRenderingAttachmentInfo
import org.lwjgl.vulkan.VkRenderingInfo
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmValue
import procmodel.model.PmModel
import procmodel.renderer.MeshDrawTask
import procmodel.renderer.PmInstance
import procmodel.renderer.config.PmRenderPassInfo
import procmodel2.Pm2dVertex
import kotlin.math.absoluteValue

private fun createDummyModel() = PmModel(listOf(
    Pm2dVertex(-0.8f, -0.7f, Color.RED, 0),
    Pm2dVertex(0.8f, -0.2f, Color.GREEN, 0),
    Pm2dVertex(-0.9f, 0.8f, Color.BLUE, 0)
), listOf(null), emptyMap())

class Pm2PreviewComponent(
    private val boiler: BoilerInstance,
    private val pmInstance: PmInstance<Pm2dVertex, Matrix3x2f>,
    initialModel: PmModel<Pm2dVertex>?, private val reportError: (String) -> Unit
): Component() {

    private var lastDynamicParameterValues: MutableMap<String, PmValue>? = null
    val dynamicParameterValues = mutableMapOf<String, PmValue>()

    private val cameraMatrix = Matrix3x2f().scale(1f, -1f)

    private var currentMesh = pmInstance.meshes.allocate(initialModel ?: createDummyModel())

    private val previewCommandPool = boiler.commands.createPool(
        VK_COMMAND_POOL_CREATE_TRANSIENT_BIT, boiler.queueFamilies().graphics.index, "Pm2dPreviewComponent"
    )
    private val previewCommandBuffer = boiler.commands.createPrimaryBuffers(
        previewCommandPool, 1, "Pm2dPreviewComponent"
    )[0]

    private val previewMatrices = pmInstance.createTransformationMatrices()
    private val previewSemaphore = boiler.sync.createSemaphores("Pm2dPreviewComponent", 1)[0]
    private val previewFence = boiler.sync.fenceBank.borrowFence()
    private var shouldAwaitFence = false

    private lateinit var previewImage: VmaImage

    fun getDynamicParameterTypes() = currentMesh.dynamicParameterTypes

    fun updateModel(newModel: PmModel<Pm2dVertex>) {
        val newMesh = pmInstance.meshes.allocate(newModel)

        if (shouldAwaitFence) {
            stackPush().use { stack -> boiler.sync.waitAndReset(stack, previewFence, 1_000_000_000L) }
            shouldAwaitFence = false
        }

        pmInstance.meshes.destroy(currentMesh)
        currentMesh = newMesh
        agent.giveFeedback(RenderFeedback())
    }

    override fun subscribeToEvents() {
        agent.subscribe(RemoveEvent::class)
        agent.subscribe(KeyPressEvent::class)
        agent.subscribe(CursorClickEvent::class)
        agent.subscribe(CursorScrollEvent::class)
        agent.subscribe(UpdateEvent::class)
    }

    override fun processEvent(event: Event) {
        if (event is CursorClickEvent) agent.giveFeedback(RequestKeyboardFocusFeedback())
        if (event is UpdateEvent) {
            if (dynamicParameterValues != lastDynamicParameterValues) {
                lastDynamicParameterValues = HashMap(dynamicParameterValues)
                agent.giveFeedback(RenderFeedback())
            }
        }
        if (event is KeyPressEvent) {
            if (event.key.type == KeyType.Left) cameraMatrix.translate(0.1f, 0f)
            if (event.key.type == KeyType.Right) cameraMatrix.translate(-0.1f, 0f)
            if (event.key.type == KeyType.Down) cameraMatrix.translate(0f, 0.1f)
            if (event.key.type == KeyType.Up) cameraMatrix.translate(0f, -0.1f)
            agent.giveFeedback(RenderFeedback())
        }
        if (event is CursorScrollEvent) {
            if (event.direction == ScrollDirection.Horizontal) cameraMatrix.translateLocal(-event.amount, 0f)
            if (event.direction == ScrollDirection.Vertical) cameraMatrix.translateLocal(0f, -event.amount)
            if (event.direction == ScrollDirection.Zoom) {
                val scale = if (event.amount >= 0f) 1f / (1f + event.amount) else 1f - event.amount
                val cursorState = agent.cursorTracker.getCursorState(event.cursor)

                if (cursorState != null) {
                    cameraMatrix.scaleAroundLocal(
                        scale,
                        2f * cursorState.localPosition.x - 1f,
                        -2f * cursorState.localPosition.y + 1f
                    )
                }
            }
            agent.giveFeedback(RenderFeedback())
        }
        if (event is RemoveEvent) {
            if (shouldAwaitFence) {
                stackPush().use { stack -> boiler.sync.waitAndReset(stack, previewFence, 1_000_000_000L) }
            }
            boiler.sync.fenceBank.returnFence(previewFence, false)
            vkDestroyCommandPool(boiler.vkDevice(), previewCommandPool, null)
            if (this::previewImage.isInitialized) {
                vkDestroyImageView(boiler.vkDevice(), previewImage.vkImageView, null)
                vmaDestroyImage(boiler.vmaAllocator(), previewImage.vkImage, previewImage.vmaAllocation)
            }
            pmInstance.meshes.destroy(currentMesh)
            previewMatrices.destroy()
            vkDestroySemaphore(boiler.vkDevice(), previewSemaphore, null)
        }
    }

    override fun render(target: GraviksTarget, force: Boolean): RenderResult {
        val aspectRatio = target.getAspectRatio()
        val cameraRatio = cameraMatrix.m11 / -cameraMatrix.m00

        if ((aspectRatio - cameraRatio).absoluteValue > 0.001f) {
            cameraMatrix.scale(cameraRatio / aspectRatio, 1f)
        }

        stackPush().use { stack ->
            try {
                if (shouldAwaitFence) boiler.sync.waitAndReset(stack, previewFence, 1_000_000_000L)

                val (width, height) = target.getSize()
                if (!this::previewImage.isInitialized || width != previewImage.width || height != previewImage.height) {
                    if (this::previewImage.isInitialized) {
                        vkDestroyImageView(boiler.vkDevice(), previewImage.vkImageView, null)
                        vmaDestroyImage(boiler.vmaAllocator(), previewImage.vkImage, previewImage.vmaAllocation)
                    }
                    previewImage = boiler.images.createSimple(
                        stack, width, height, VK_FORMAT_R8G8B8A8_SRGB,
                        VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK_IMAGE_USAGE_SAMPLED_BIT,
                        VK_IMAGE_ASPECT_COLOR_BIT, "Pm2dPreview"
                    )
                }

                assertVkSuccess(vkResetCommandPool(
                    boiler.vkDevice(), previewCommandPool, 0
                ), "ResetCommandPool", "Pm2dPreview")

                val commands = CommandRecorder.begin(previewCommandBuffer, boiler, stack, "Pm2dPreview")

                commands.transitionColorLayout(
                    previewImage.vkImage, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    null, ResourceUsage(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                )

                val colorAttachments = VkRenderingAttachmentInfo.calloc(1, stack)
                colorAttachments.`sType$Default`()
                colorAttachments.imageView(previewImage.vkImageView)
                colorAttachments.imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                colorAttachments.resolveMode(VK_RESOLVE_MODE_NONE)
                colorAttachments.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                colorAttachments.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                colorAttachments.clearValue().color().float32().put(0f).put(0.2f).put(0.6f).put(1f)

                val biRender = VkRenderingInfo.calloc(stack)
                biRender.`sType$Default`()
                biRender.renderArea().extent().set(previewImage.width, previewImage.height)
                biRender.layerCount(1)
                biRender.pColorAttachments(colorAttachments)

                vkCmdBeginRendering(previewCommandBuffer, biRender)

                val renderPassInfo = PmRenderPassInfo.dynamicRendering(
                    { ciRendering, _ ->
                        ciRendering.colorAttachmentCount(1)
                        ciRendering.pColorAttachmentFormats(stack.ints(VK_FORMAT_R8G8B8A8_SRGB))
                    }, { ciPipeline, innerStack ->
                        GraphicsPipelineBuilder(ciPipeline, boiler, innerStack).simpleColorBlending(1)
                    }
                )

                previewMatrices.computeAndStore(listOf(MeshDrawTask(currentMesh, dynamicParameterValues)))

                pmInstance.commands.recordDrawingCommands(
                    previewCommandBuffer, renderPassInfo, previewMatrices.descriptorSet,
                    previewImage.width, previewImage.height, listOf(Pair(currentMesh, 0)), cameraMatrix
                )
                vkCmdEndRendering(previewCommandBuffer)

                commands.transitionColorLayout(
                    previewImage.vkImage, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    ResourceUsage(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
                    ResourceUsage(VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
                )
                commands.end()

                boiler.queueFamilies().graphics.queues[0].submit(
                    previewCommandBuffer, "Pm2dPreviewComponent", emptyArray(), previewFence, previewSemaphore
                )
                target.addWaitSemaphore(previewSemaphore, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
                target.drawVulkanImage(0f, 0f, 1f, 1f, previewImage.vkImage, previewImage.vkImageView)
                shouldAwaitFence = true
            } catch (runtimeError: PmRuntimeError) {
                reportError(runtimeError.message!!)
            }
        }

        return RenderResult(
            drawnRegion = RectangularDrawnRegion(0f, 0f, 1f, 1f),
            propagateMissedCursorEvents = false
        )
    }
}
