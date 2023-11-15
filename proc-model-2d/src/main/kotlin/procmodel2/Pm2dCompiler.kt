package procmodel2

import procmodel.compiler.PmCompiler
import procmodel.importer.PmClassPathImportFunctions
import procmodel.importer.PmDummyImportFunctions
import procmodel.importer.PmImportCache
import procmodel.importer.PmImporter
import procmodel.program.PmProgram
import java.util.*

object Pm2dCompiler {

    val extraFunctions = mapOf(
        Pair("translate", 3),
        Pair("rotate", 2),
        Pair("scale", 3)
    )

    fun compile(sourceCode: String, importer: PmImporter<Pm2dVertexValue>) = PmCompiler.compile(
        sourceCode, importer, extraFunctions + mapOf(Pair("produceTriangle", 3)), Pm2dTypes.all
    )

    fun compile(sourceCode: String) = compile(sourceCode, PmImporter(
        PmImportCache(PmDummyImportFunctions()), "") { _ -> Pm2dVertexValue() }
    )

    fun createClassPathImporter(prefix: String) = PmImporter(
        PmImportCache(PmClassPathImportFunctions(prefix)), "", Pm2dShapes::parseVertex
    )

    fun compileFromClassPath(prefix: String, mainFile: String): PmProgram {
        val importer = createClassPathImporter(prefix)

        val sourceCode = StringBuilder()
        val sourceCodeScanner = Scanner(PmImporter::class.java.classLoader.getResourceAsStream("$prefix/$mainFile.pm2"))
        while (sourceCodeScanner.hasNextLine()) {
            sourceCode.appendLine(sourceCodeScanner.nextLine())
        }
        sourceCodeScanner.close()

        return compile(sourceCode.toString(), importer)
    }
}
