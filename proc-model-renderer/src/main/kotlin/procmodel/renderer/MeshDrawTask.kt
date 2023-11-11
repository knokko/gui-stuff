package procmodel.renderer

import procmodel.lang.types.PmValue

class MeshDrawTask(
    val mesh: PmMesh,
    val dynamicParameters: Map<String, PmValue>
)
