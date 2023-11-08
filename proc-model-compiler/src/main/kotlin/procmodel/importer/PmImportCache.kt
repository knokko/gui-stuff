package procmodel.importer

import procmodel.compiler.PmChildProgramBuilder
import procmodel.lang.types.PmValue
import procmodel.program.PmChildProgram

class PmImportCache<VertexValue>(val importFunctions: PmImportFunctions) {

    internal val values = mutableMapOf<String, PmValue>()

    internal val models = mutableMapOf<String, PmChildProgramBuilder>()

    internal val triangles = mutableMapOf<String, Pair<Set<VertexValue>, List<VertexValue>>>()
}
