package procmodel.renderer.config

import java.nio.ByteBuffer

class PmVertexInfo<Vertex>(
    val byteSize: Int,
    val populate: (Vertex, ByteBuffer) -> Unit,
)
