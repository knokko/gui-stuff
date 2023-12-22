package procmodel.processor

import org.joml.Vector2f
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import procmodel.lang.functions.PmBuiltinFunction
import procmodel.lang.types.*
import procmodel.lang.types.hints.PmFloatRangeHint
import procmodel.program.*
import procmodel.test.*

class TestVertexProcessor {

    @Test
    fun testSomewhatComplicatedProgram() {
        val rawStaticParameters = PmMap()
        rawStaticParameters[PmString("ownColor")] = PmColor(0f, 0f, 1f)
        val processor = PmVertexProcessor<Vertex, FinalVertex>(PmTestCase.program, rawStaticParameters, PmTestCase.vertexType) {
            vertex -> FinalVertex(vertex)
        }
        processor.addBuiltinFunction("constructPosition", PmBuiltinFunction(
            listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT), PmTestCase.positionType) {
            parameters -> Position(parameters[0].floatValue(), parameters[1].floatValue())
        })

        val model = processor.execute()
        assertEquals(mapOf(
            Pair("passDistance", PmFatType(PmBuiltinTypes.FLOAT, PmFloatRangeHint(0f, 1f)))
        ), model.dynamicParameters)

        val red = PmColor(1f, 0f, 0f)
        val green = PmColor(0f, 1f, 0f)
        val blue = PmColor(0f, 0f, 1f)

        val bottomLeft = Position(-1f, -1f)
        val bottomRight = Position(1f, -1f)
        val topMid = Position(0f, 1f)

        assertArrayEquals(listOf(
            FinalVertex(bottomLeft, blue, 1),
            FinalVertex(bottomRight, blue, 1),
            FinalVertex(topMid, blue, 1),

            FinalVertex(bottomLeft, red, 3),
            FinalVertex(bottomRight, red, 3),
            FinalVertex(topMid, red, 3),

            FinalVertex(bottomLeft, green, 4),
            FinalVertex(bottomRight, green, 4),
            FinalVertex(topMid, green, 4)
        ).toTypedArray(), model.vertices.toTypedArray())

        assertEquals(5, model.matrices.size)
        assertNull(model.matrices[0])

        val bigMatrix = model.matrices[1]!!
        assertEquals(0, bigMatrix.parentIndex)
        assertEquals(mapOf(
            Pair("passDistance", PmFatType(PmBuiltinTypes.FLOAT, PmFloatRangeHint(0f, 1f)))
        ), bigMatrix.dynamicParameterTypes)
        assertNull(bigMatrix.propagator)
        assertTrue(bigMatrix.transferredVariables.isEmpty())
        assertEquals(PmTestCase.dynamicMatrices[0].instructions, bigMatrix.construction.instructions)

        val smallMatrix = model.matrices[2]!!
        assertEquals(0, smallMatrix.parentIndex)
        assertEquals(mapOf(
            Pair("passDistance", PmFatType(PmBuiltinTypes.FLOAT, PmFloatRangeHint(0f, 1f)))
        ), smallMatrix.dynamicParameterTypes)
        assertNull(smallMatrix.propagator)
        assertTrue(smallMatrix.transferredVariables.isEmpty())
        assertEquals(PmTestCase.dynamicMatrices[1].instructions, smallMatrix.construction.instructions)

        val downMatrix = model.matrices[3]!!
        assertEquals(2, downMatrix.parentIndex)
        assertEquals(mapOf(Pair("distance", PmFatType(PmBuiltinTypes.FLOAT, null))), downMatrix.dynamicParameterTypes)
        assertEquals(PmTestCase.childInvocations[1].instructions, downMatrix.propagator!!.instructions)
        assertEquals(mapOf(
            Pair("offsetX", Pair(PmBuiltinTypes.FLOAT, PmFloat(0.2f))),
            Pair("offsetY", Pair(PmBuiltinTypes.FLOAT, PmFloat(-0.3f)))
        ), downMatrix.transferredVariables)
        assertEquals(PmTestCase.dynamicMatrices[2].instructions, downMatrix.construction.instructions)

        val upMatrix = model.matrices[4]!!
        assertEquals(2, upMatrix.parentIndex)
        assertEquals(mapOf(Pair("distance", PmFatType(PmBuiltinTypes.FLOAT, null))), upMatrix.dynamicParameterTypes)
        assertEquals(PmTestCase.childInvocations[1].instructions, upMatrix.propagator!!.instructions)
        assertEquals(mapOf(
            Pair("offsetX", Pair(PmBuiltinTypes.FLOAT, PmFloat(0.2f))),
            Pair("offsetY", Pair(PmBuiltinTypes.FLOAT, PmFloat(-0.3f)))
        ), upMatrix.transferredVariables)
        assertEquals(PmTestCase.dynamicMatrices[3].instructions, upMatrix.construction.instructions)

        val translate = PmBuiltinFunction(listOf(
            PmTestCase.matrixType, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT
        ), PmBuiltinTypes.VOID) { parameters ->
            parameters[0].castTo<Matrix>().inner.translate(parameters[1].floatValue(), parameters[2].floatValue())
            PmNone()
        }

        // Sanity check that the matrices can actually be executed
        val matrixProcessor = PmMatrixProcessor(upMatrix, mapOf(Pair("distance", PmFloat(2f))))
        matrixProcessor.addBuiltinFunction("translate", translate)
        matrixProcessor.execute()
        val result = matrixProcessor.result!!.castTo<Matrix>().inner
        val projection = result.transformPosition(Vector2f())
        assertEquals(0.2f, projection.x, 0.001f)
        assertEquals(1.7f, projection.y, 0.001f)

        val propagationProcessor = PmDynamicParameterProcessor(
            PmProgramBody(upMatrix.propagator!!.instructions),
            mapOf(Pair("passDistance", PmFloat(0.25f))),
            mapOf(Pair("passDistance", PmFatType(PmBuiltinTypes.FLOAT, PmFloatRangeHint(0f, 1f))))
        )
        propagationProcessor.execute()
        val propResult = propagationProcessor.result as PmMap
        assertEquals(1, propResult.map.size)
        assertEquals(PmFloat(0.25f), propResult[PmString("distance")])
    }
}
