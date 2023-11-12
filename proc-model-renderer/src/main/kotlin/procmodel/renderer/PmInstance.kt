package procmodel.renderer

import com.github.knokko.boiler.instance.BoilerInstance
import procmodel.renderer.config.PmModelInfo

/**
 * The root of the renderer system.
 */
class PmInstance<Vertex, Matrix>(
    private val boiler: BoilerInstance,
    private val modelInfo: PmModelInfo<Vertex, Matrix>
) {
    val meshes = PmMeshes(boiler, modelInfo.vertices)
    val commands = PmCommands(boiler, modelInfo.pipeline)

    fun createTransformationMatrices() = PmTransformationMatrices(
        boiler, modelInfo.matrices,
        modelInfo.pipeline,
        modelInfo.builtinFunctions
    )

    fun destroy() {
        meshes.destroy()
        modelInfo.destroy(boiler.vkDevice())
        commands.destroy()
    }
}
