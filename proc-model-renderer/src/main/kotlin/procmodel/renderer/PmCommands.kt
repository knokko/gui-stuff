package procmodel.renderer

import com.github.knokko.boiler.commands.CommandRecorder
import com.github.knokko.boiler.instance.BoilerInstance
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkCommandBuffer
import org.lwjgl.vulkan.VkDevice
import procmodel.exceptions.PmRuntimeError
import procmodel.renderer.config.PmPipelineInfo
import procmodel.renderer.config.PmRenderPassInfo
import java.util.concurrent.ConcurrentHashMap

class PmCommands<Vertex, Matrix> internal constructor(
    private val boiler: BoilerInstance,
    private val pipelineInfo: PmPipelineInfo<Vertex, Matrix>
) {

    private val pipelines = ConcurrentHashMap<PmRenderPassInfo, Long>()

    @Throws(PmRuntimeError::class)
    fun recordDrawingCommands(
        commandBuffer: VkCommandBuffer, renderPassInfo: PmRenderPassInfo, descriptorSet: Long,
        viewportWidth: Int, viewportHeight: Int,
        meshesWithMatrices: List<Pair<PmMesh, Int>>, cameraMatrix: Matrix
    ) {
        val pipeline = pipelines.computeIfAbsent(renderPassInfo, pipelineInfo.createGraphicsPipeline)

        stackPush().use { stack ->
            val pushBuffer = stack.callocInt(1)
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline)
            CommandRecorder.alreadyRecording(commandBuffer, boiler, stack).dynamicViewportAndScissor(viewportWidth, viewportHeight);

            vkCmdBindDescriptorSets(
                commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineInfo.pipelineLayout,
                0, stack.longs(descriptorSet), null
            )

            pipelineInfo.pushCameraMatrix(cameraMatrix, stack, commandBuffer)

            val vertexBuffers = mutableSetOf<Long>()
            for (mesh in meshesWithMatrices) vertexBuffers.add(mesh.first.vertexBuffer.vkBuffer)

            for (vertexBuffer in vertexBuffers) {
                val currentMeshes = meshesWithMatrices.filter { it.first.vertexBuffer.vkBuffer == vertexBuffer }
                vkCmdBindVertexBuffers(commandBuffer, 0, stack.longs(vertexBuffer), stack.longs(0))
                // TODO Experiment with optimizations
                for ((mesh, matrixIndexOffset) in currentMeshes) {

                    pushBuffer.put(0, matrixIndexOffset)
                    pipelineInfo.pushMatrixIndexOffset(pushBuffer, commandBuffer)
                    vkCmdDraw(commandBuffer, mesh.numVertices, 1, mesh.vertexOffset, 0)
                }
            }
        }
    }

    fun destroy() {
        pipelines.values.forEach { pipeline ->
            vkDestroyPipeline(boiler.vkDevice(), pipeline, null)
        }
    }
}
