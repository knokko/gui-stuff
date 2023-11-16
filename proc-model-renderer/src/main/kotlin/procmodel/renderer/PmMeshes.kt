package procmodel.renderer

import com.github.knokko.boiler.instance.BoilerInstance
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.vma.Vma.vmaDestroyBuffer
import org.lwjgl.vulkan.VK10
import procmodel.model.PmModel
import procmodel.renderer.config.PmVertexInfo

/**
 * The renderer component responsible for (de)allocating `PmMesh`s.
 */
class PmMeshes<Vertex> internal constructor(
    private val boiler: BoilerInstance,
    private val vertexInfo: PmVertexInfo<Vertex>
) {
    fun allocate(model: PmModel<Vertex>): PmMesh {
        // OPTIMIZE Buffer sub-allocation
        val vertexBuffer = boiler.buffers.createMapped(
            model.vertices.size * vertexInfo.byteSize.toLong(),
            VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, "PmMeshVertexBuffer"
        )

        val byteBuffer = MemoryUtil.memByteBuffer(
            vertexBuffer.hostAddress, model.vertices.size * vertexInfo.byteSize
        )
        for (vertex in model.vertices) {
            vertexInfo.populate(vertex, byteBuffer)
        }

        return PmMesh(
            vertexBuffer = vertexBuffer.asBuffer(), vertexOffset = 0,
            numVertices = model.vertices.size, matrices = model.matrices,
            dynamicParameterTypes = model.dynamicParameters
        )
    }

    fun destroy(mesh: PmMesh) {
        vmaDestroyBuffer(boiler.vmaAllocator(), mesh.vertexBuffer.vkBuffer, mesh.vertexBuffer.vmaAllocation)
    }

    fun destroy() {

    }
}
