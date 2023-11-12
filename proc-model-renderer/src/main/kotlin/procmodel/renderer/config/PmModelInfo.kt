package procmodel.renderer.config

import org.lwjgl.vulkan.VkDevice
import procmodel.lang.functions.PmBuiltinFunction

class PmModelInfo<Vertex, Matrix>(
    val vertices: PmVertexInfo<Vertex>,
    val matrices: PmMatrixInfo<Matrix>,
    val pipeline: PmPipelineInfo<Matrix>,

    val builtinFunctions: Map<String, PmBuiltinFunction>,
) {
    fun destroy(vkDevice: VkDevice) {
        pipeline.destroy(vkDevice)
    }
}
