package procmodel.importer

import procmodel.compiler.PmChildProgramBuilder
import procmodel.compiler.PmCompiler
import procmodel.exceptions.PmCompileError
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmInt
import procmodel.lang.types.PmValue
import procmodel.processor.PmValueProcessor

class PmImporter<VertexValue : PmValue>(
    internal val cache: PmImportCache<VertexValue>,
    private val prefix: String,
    private val parseVertex: (Map<String, Any>) -> VertexValue
) {

    @Throws(PmCompileError::class, PmRuntimeError::class)
    fun importValue(path: String, isRelative: Boolean, extraFunctions: Map<String, Int>): PmValue {
        val fullPath = (if (isRelative) prefix + path else path) + ".pv2"
        val cachedValue = cache.values[fullPath]

        if (cachedValue != null) return cachedValue.copy()

        val sourceCode = cache.importFunctions.getSourceCode(fullPath) ?: throw PmCompileError("Can't import $fullPath")

        val lastSlash = fullPath.lastIndexOf('/')
        val valuePrefix = if (lastSlash == -1) "" else fullPath.substring(0 until lastSlash)
        val valueImporter = PmImporter(cache, valuePrefix, parseVertex)

        val valueProgram = PmCompiler.compile(sourceCode, valueImporter, extraFunctions)

        val processor = PmValueProcessor(valueProgram.body)
        processor.execute()
        cache.values[fullPath] = processor.result!!
        return processor.result!!
    }

    @Throws(PmCompileError::class)
    fun importModel(path: String, isRelative: Boolean, extraFunctions: Map<String, Int>): String {
        val fullPath = (if (isRelative) prefix + path else path) + ".pm2"
        val cachedId = cache.models[fullPath]

        if (cachedId != null) return cachedId.id

        val sourceCode = cache.importFunctions.getSourceCode(fullPath) ?: throw PmCompileError("Can't import $fullPath")
        val lastSlash = fullPath.lastIndexOf('/')
        val valuePrefix = if (lastSlash == -1) "" else fullPath.substring(0 until lastSlash)
        val childImporter = PmImporter(cache, valuePrefix, parseVertex)

        val childProgram = PmChildProgramBuilder(fullPath) // The path is a good ID, at least for now

        // IMPORTANT: To avoid endless recursion when two child programs import each other,
        // the compilations must happen AFTER the child program is stored in the cache
        cache.models[fullPath] = childProgram
        childProgram.program = PmCompiler.compile(sourceCode, childImporter, extraFunctions, true)

        return childProgram.id
    }

    @Throws(PmCompileError::class)
    fun importTriangles(path: String, isRelative: Boolean): Pair<Set<VertexValue>, List<VertexValue>> {
        val fullPath = (if (isRelative) prefix + path else path) + ".tri2"
        val cachedTriangles = cache.triangles[fullPath]
        if (cachedTriangles != null) return cachedTriangles

        val input = cache.importFunctions.getBinaryInput(fullPath) ?: throw PmCompileError("Can't import $fullPath")
        val importedTriangles = parseShape(input, parseVertex)
        input.close()

        cache.triangles[fullPath] = importedTriangles
        return importedTriangles
    }

    companion object {
        fun dummy() = PmImporter(PmImportCache(PmDummyImportFunctions()), "") { PmInt(43) }
    }
}
