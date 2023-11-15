package procmodel.renderer

import com.github.knokko.boiler.buffer.VmaBuffer
import procmodel.lang.types.PmType
import procmodel.model.PmDynamicMatrix

class PmMesh internal constructor(
    internal val vertexBuffer: VmaBuffer,
    internal val vertexOffset: Int,
    internal val numVertices: Int,
    internal val matrices: List<PmDynamicMatrix?>,
    val dynamicParameterTypes: Map<String, PmType>
)
