package procmodel.renderer

import com.github.knokko.boiler.instance.BoilerInstance
import procmodel.renderer.config.PmPipelineInfo

/**
 * The root of the renderer system.
 */
class PmInstance<Vertex, Matrix>(
    private val boiler: BoilerInstance,
    private val pipelineInfo: PmPipelineInfo<Vertex, Matrix>
) {
    val meshes = PmMeshes(boiler, pipelineInfo.vertices)
    val commands = PmCommands(boiler, pipelineInfo)
    val transformationMatrices = PmTransformationMatrices(
        boiler, pipelineInfo.matrices,
        pipelineInfo.createDescriptorPool,
        pipelineInfo.descriptorSetLayout
    )

    fun destroy() {
        transformationMatrices.destroy()
        meshes.destroy()
        pipelineInfo.destroy(boiler.vkDevice())
        commands.destroy()
    }
}
