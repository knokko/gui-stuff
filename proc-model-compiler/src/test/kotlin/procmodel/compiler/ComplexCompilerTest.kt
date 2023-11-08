package procmodel.compiler

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import procmodel.importer.PmClassPathImportFunctions
import procmodel.importer.PmImportCache
import procmodel.importer.PmImporter
import procmodel.lang.types.PmColor
import procmodel.lang.types.PmMatrixIndex
import procmodel.test.PmTestCase
import procmodel.test.Position
import procmodel.test.Vertex

class ComplexCompilerTest {

    private fun parseVertex(attributes: Map<String, Any>): Vertex {
        return Vertex(
            Position(attributes["x"] as Float, attributes["y"] as Float),
            attributes["color"] as PmColor,
            attributes["matrix"] as PmMatrixIndex
        )
    }

    @Test
    fun testCompileSomewhatComplicatedTestCase() {
        val classPathImporter = PmClassPathImportFunctions("procmodel/test")

        val sourceCode = classPathImporter.getSourceCode("/minimal.pm2")!!

        val program = PmCompiler.compile(
            sourceCode,
            PmImporter(PmImportCache(classPathImporter), "", ::parseVertex),
            mapOf(Pair("produceTriangle", 3), Pair("translate", 3), Pair("scale", 3)),
            listOf(PmTestCase.vertexType, PmTestCase.matrixType)
        )

        assertArrayEquals(PmTestCase.program.body.instructions.toTypedArray(), program.body.instructions.toTypedArray())
        for ((index, expected) in PmTestCase.program.dynamicMatrices.withIndex()) {
            val actual = program.dynamicMatrices[index]
            assertArrayEquals(expected.instructions.toTypedArray(), actual.instructions.toTypedArray())
        }
        for ((index, expected) in PmTestCase.program.childInvocations.withIndex()) {
            val actual = program.childInvocations[index]
            // TODO Remove the unneeded .dynamicParameters.instructions chaining
            assertArrayEquals(expected.dynamicParameters.instructions.toTypedArray(), actual.dynamicParameters.instructions.toTypedArray())
        }
        assertEquals(PmTestCase.program.children, program.children)
        assertEquals(PmTestCase.program.staticParameters, program.staticParameters)
        assertEquals(PmTestCase.program.dynamicParameters, program.dynamicParameters)
    }
}
