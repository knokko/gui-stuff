package procmodel.model

import procmodel.lang.types.PmType

class PmModel<V>(
    val vertices: List<V>,
    val matrices: List<PmDynamicMatrix?>,
    val dynamicParameters: Map<String, PmType>
)
