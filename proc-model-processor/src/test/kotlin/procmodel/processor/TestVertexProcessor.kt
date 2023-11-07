package procmodel.processor

import org.joml.Matrix3x2f
import org.joml.Vector2f
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.functions.PmBuiltinFunction
import procmodel.lang.instructions.PmInstruction.Companion.add
import procmodel.lang.instructions.PmInstruction.Companion.createChildModel
import procmodel.lang.instructions.PmInstruction.Companion.createDynamicMatrix
import procmodel.lang.instructions.PmInstruction.Companion.declareVariable
import procmodel.lang.instructions.PmInstruction.Companion.delete
import procmodel.lang.instructions.PmInstruction.Companion.exitProgram
import procmodel.lang.instructions.PmInstruction.Companion.invokeBuiltinFunction
import procmodel.lang.instructions.PmInstruction.Companion.pushValue
import procmodel.lang.instructions.PmInstruction.Companion.pushVariable
import procmodel.lang.instructions.PmInstruction.Companion.setProperty
import procmodel.lang.instructions.PmInstruction.Companion.subtract
import procmodel.lang.instructions.PmInstruction.Companion.transferVariable
import procmodel.lang.instructions.PmInstruction.Companion.updateListOrMap
import procmodel.lang.types.*
import procmodel.program.*

class TestVertexProcessor {

    private class Position(val x: Float, val y: Float) : PmValue() {
        override fun copy() = this

        override fun toString() = "($x, $y)"

        override fun equals(other: Any?) = other is Position && x == other.x && y == other.y

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            return result
        }
    }

    private class Vertex(var position: Position?, var color: PmColor, var matrix: PmMatrixIndex) : PmValue() {
        override fun copy() = Vertex(position, color, matrix)

        override fun getProperty(propertyName: String): PmValue {
            return when (propertyName) {
                "position" -> position ?: throw PmRuntimeError("Position hasn't been specified yet")
                "color" -> color
                "matrix" -> matrix
                else -> throw PmRuntimeError("Unknown property Vertex.$propertyName")
            }
        }

        override fun setProperty(propertyName: String, newValue: PmValue) {
            when (propertyName) {
                "position" -> position = newValue.castTo()
                "color" -> color = newValue.castTo()
                "matrix" -> matrix = newValue.castTo()
            }
        }
    }

    private class Matrix(val inner: Matrix3x2f) : PmValue() {
        override fun copy() = Matrix(Matrix3x2f(inner))
    }

    private class FinalVertex(val position: Position, val color: PmColor, val matrixIndex: Int) {
        constructor(vertex: Vertex) : this(vertex.position!!, vertex.color, vertex.matrix.index)

        override fun toString() = "Vertex($position, $color, $matrixIndex)"

        override fun equals(other: Any?) = other is FinalVertex && position == other.position &&
                color == other.color && matrixIndex == other.matrixIndex

        override fun hashCode(): Int {
            var result = position.hashCode()
            result = 31 * result + color.hashCode()
            result = 31 * result + matrixIndex
            return result
        }
    }

    @Test
    fun testSomewhatComplicatedProgram() {
        val vertexType = PmType("Vertex", null, Vertex::class)
        val matrixType = PmType("Matrix", null, Matrix::class)
        val positionType = PmType("position", null, Position::class)

        val mainBody = listOf(
            pushValue(PmMap(), 8), // index 0
            declareVariable("triangleParameters", PmBuiltinTypes.MAP, 8), // index 1
            pushVariable("triangleParameters", 9), // index 2
            pushValue(PmString("color"), 9), // index 3
            pushVariable("ownColor", 9), // index 4
            updateListOrMap(9), // index 5
            pushValue(PmInt(0), 11), // index 6
            createDynamicMatrix(11), // index 7
            declareVariable("bigMatrix", PmBuiltinTypes.MATRIX_INDEX, 11), // index 8
            pushVariable("bigMatrix", 17), // index 9
            pushVariable("triangleParameters", 17), // index 10
            pushValue(PmInt(0), 17), // index 11
            createChildModel("triangle", 17), // index 12
            pushValue(PmMap(), 19), // index 13
            declareVariable("nestedParameters", PmBuiltinTypes.MAP, 19), // index 14
            pushVariable("nestedParameters", 20), // index 15
            pushValue(PmString("offsetX"), 20), // index 16
            pushValue(PmFloat(0.2f), 20), // index 17
            updateListOrMap(20), // index 18
            pushVariable("nestedParameters", 21), // index 19
            pushValue(PmString("offsetY"), 21), // index 20
            pushValue(PmFloat(-0.3f), 21), // index 21
            updateListOrMap(21), // index 22
            pushVariable("nestedParameters", 22), // index 23
            pushValue(PmString("color1"), 22), // index 24
            pushValue(PmFloat(1.0f), 22), // index 25
            pushValue(PmFloat(0.0f), 22), // index 26
            pushValue(PmFloat(0.0f), 22), // index 27
            invokeBuiltinFunction("rgb", 22), // index 28
            updateListOrMap(22), // index 29
            pushVariable("nestedParameters", 23), // index 30
            pushValue(PmString("color2"), 23), // index 31
            pushValue(PmFloat(0.0f), 23), // index 32
            pushValue(PmFloat(1.0f), 23), // index 33
            pushValue(PmFloat(0.0f), 23), // index 34
            invokeBuiltinFunction("rgb", 23), // index 35
            updateListOrMap(23), // index 36
            pushValue(PmInt(1), 25), // index 37
            createDynamicMatrix(25), // index 38
            declareVariable("smallMatrix", PmBuiltinTypes.MATRIX_INDEX, 25), // index 39
            pushVariable("smallMatrix", 30), // index 40
            pushVariable("nestedParameters", 30), // index 41
            pushValue(PmInt(1), 30), // index 42
            createChildModel("nested", 30), // index 43
            exitProgram(35), // index 44
        )
        val nestedBody = listOf(
            pushValue(PmMap(), 10), // index 44
            declareVariable("childParameters1", PmBuiltinTypes.MAP, 10), // index 45
            pushVariable("childParameters1", 11), // index 46
            pushValue(PmString("color"), 11), // index 47
            pushVariable("color1", 11), // index 48
            updateListOrMap(11), // index 49
            pushValue(PmMap(), 13), // index 50
            declareVariable("childParameters2", PmBuiltinTypes.MAP, 13), // index 51
            pushVariable("childParameters2", 14), // index 52
            pushValue(PmString("color"), 14), // index 53
            pushVariable("color2", 14), // index 54
            updateListOrMap(14), // index 55
            transferVariable("offsetX", PmBuiltinTypes.FLOAT, 16), // index 56
            transferVariable("offsetY", PmBuiltinTypes.FLOAT, 16), // index 57
            pushValue(PmInt(2), 16), // index 58
            createDynamicMatrix(16), // index 59
            declareVariable("downMatrix", PmBuiltinTypes.MATRIX_INDEX, 16), // index 60
            transferVariable("offsetX", PmBuiltinTypes.FLOAT, 21), // index 61
            transferVariable("offsetY", PmBuiltinTypes.FLOAT, 21), // index 62
            pushValue(PmInt(3), 21), // index 63
            createDynamicMatrix(21), // index 64
            declareVariable("upMatrix", PmBuiltinTypes.MATRIX_INDEX, 21), // index 65
            pushVariable("downMatrix", 26), // index 66
            pushVariable("childParameters1", 26), // index 67
            pushValue(PmInt(2), 26), // index 68
            createChildModel("triangle", 26), // index 69
            pushVariable("upMatrix", 27), // index 70
            pushVariable("childParameters2", 27), // index 71
            pushValue(PmInt(3), 27), // index 72
            createChildModel("triangle", 27), // index 73
            exitProgram(28) // index 74
        )
        val triangleBody = listOf(
            pushValue(Vertex(null, PmColor(0.0f, 0.0f, 0.0f), PmMatrixIndex(0)), 3), // index 75
            declareVariable("bottomLeft", vertexType, 3), // index 76
            pushVariable("bottomLeft", 4), // index 77
            pushValue(PmFloat(-1.0f), 4), // index 78
            pushValue(PmFloat(-1.0f), 4), // index 79
            invokeBuiltinFunction("constructPosition", 4), // index 80
            setProperty("position", 4), // index 81
            pushVariable("bottomLeft", 5), // index 82
            pushVariable("color", 5), // index 83
            setProperty("color", 5), // index 84
            pushValue(Vertex(null, PmColor(0.0f, 0.0f, 0.0f), PmMatrixIndex(0)), 7), // index 85
            declareVariable("bottomRight", vertexType, 7), // index 86
            pushVariable("bottomRight", 8), // index 87
            pushValue(PmFloat(1.0f), 8), // index 88
            pushValue(PmFloat(-1.0f), 8), // index 89
            invokeBuiltinFunction("constructPosition", 8), // index 90
            setProperty("position", 8), // index 91
            pushVariable("bottomRight", 9), // index 92
            pushVariable("color", 9), // index 93
            setProperty("color", 9), // index 94
            pushValue(Vertex(null, PmColor(0.0f, 0.0f, 0.0f), PmMatrixIndex(0)), 11), // index 95
            declareVariable("top", vertexType, 11), // index 96
            pushVariable("top", 12), // index 97
            pushValue(PmFloat(0.0f), 12), // index 98
            pushValue(PmFloat(1.0f), 12), // index 99
            invokeBuiltinFunction("constructPosition", 12), // index 100
            setProperty("position", 12), // index 101
            pushVariable("top", 13), // index 102
            pushVariable("color", 13), // index 103
            setProperty("color", 13), // index 104
            pushVariable("bottomLeft", 15), // index 105
            pushVariable("bottomRight", 15), // index 106
            pushVariable("top", 15), // index 107
            invokeBuiltinFunction("produceTriangle", 15), // index 108
            delete(15), // index 109
            exitProgram(16), // index 110
        )
        val dynamicMatrices = listOf(
            PmDynamicMatrixConstructor(listOf(
                pushValue(Matrix(Matrix3x2f()), 12),
                declareVariable("bigMatrix", matrixType, 12),
                pushVariable("bigMatrix", 13),
                pushValue(PmFloat(4.0f), 13),
                pushValue(PmFloat(4.0f), 13),
                invokeBuiltinFunction("scale", 13),
                delete(13),
                pushVariable("bigMatrix", 14),
                invokeBuiltinFunction("outputValue", 14),
                delete(14),
                exitProgram(15),
            )),
            PmDynamicMatrixConstructor(listOf(
                pushValue(Matrix(Matrix3x2f()), 26),
                declareVariable("smallMatrix", matrixType, 26),
                pushVariable("smallMatrix", 27),
                pushValue(PmFloat(0.4f), 27),
                pushValue(PmFloat(0.4f), 27),
                invokeBuiltinFunction("scale", 27),
                delete(27),
                pushVariable("smallMatrix", 28),
                invokeBuiltinFunction("outputValue", 28),
                delete(28),
                exitProgram(29)
            )),
            PmDynamicMatrixConstructor(listOf(
                pushValue(Matrix(Matrix3x2f()), 17),
                declareVariable("downMatrix", matrixType, 17),
                pushVariable("downMatrix", 18),
                pushVariable("offsetX", 18),
                pushVariable("offsetY", 18),
                pushVariable("distance", 18),
                subtract(18),
                invokeBuiltinFunction("translate", 18),
                delete(18),
                pushVariable("downMatrix", 19),
                invokeBuiltinFunction("outputValue", 19),
                delete(19),
                exitProgram(20)
            )),
            PmDynamicMatrixConstructor(listOf(
                pushValue(Matrix(Matrix3x2f()), 22),
                declareVariable("upMatrix", matrixType, 22),
                pushVariable("upMatrix", 23),
                pushVariable("offsetX", 23),
                pushVariable("offsetY", 23),
                pushVariable("distance", 23),
                add(23),
                invokeBuiltinFunction("translate", 23),
                delete(23),
                pushVariable("upMatrix", 24),
                invokeBuiltinFunction("outputValue", 24),
                delete(24),
                exitProgram(25)
            ))
        )
        val childInvocations = listOf(
            PmChildProgramInvocation(PmParameterPropagator(listOf(
                pushValue(PmMap(), 17),
                exitProgram(17)
            ))),
            PmChildProgramInvocation(PmParameterPropagator(listOf(
                pushValue(PmMap(), 27),
                declareVariable("parameters", PmBuiltinTypes.MAP, 27),
                pushVariable("parameters", 28),
                pushValue(PmString("distance"), 28),
                pushVariable("passDistance", 28),
                updateListOrMap(28),
                pushVariable("parameters", 29),
                invokeBuiltinFunction("outputValue", 29),
                delete(29),
                exitProgram(30)
            ))),
            PmChildProgramInvocation(PmParameterPropagator(listOf(
                pushValue(PmMap(), 26),
                invokeBuiltinFunction("outputValue", 26),
                delete(26),
                exitProgram(26)
            ))),
            PmChildProgramInvocation(PmParameterPropagator(listOf(
                pushValue(PmMap(), 27),
                invokeBuiltinFunction("outputValue", 27),
                delete(27),
                exitProgram(27)
            )))
        )
        val children = mapOf(
            Pair("triangle", PmChildProgram(
                mainBody.size + nestedBody.size, mapOf(Pair("color", PmBuiltinTypes.COLOR)), emptyMap()
            )),
            Pair("nested", PmChildProgram(mainBody.size, mapOf(
                Pair("offsetX", PmBuiltinTypes.FLOAT),
                Pair("offsetY", PmBuiltinTypes.FLOAT),
                Pair("color1", PmBuiltinTypes.COLOR),
                Pair("color2", PmBuiltinTypes.COLOR)
            ), mapOf(Pair("distance", PmBuiltinTypes.FLOAT))))
        )
        val staticParameterTypes = mapOf(Pair("ownColor", PmBuiltinTypes.COLOR))
        val dynamicParameterTypes = mapOf(Pair("passDistance", PmBuiltinTypes.FLOAT))
        val program = PmProgram(
            PmProgramBody(mainBody + nestedBody + triangleBody),
            dynamicMatrices,
            childInvocations,
            children,
            staticParameterTypes,
            dynamicParameterTypes
        )

        val rawStaticParameters = PmMap()
        rawStaticParameters[PmString("ownColor")] = PmColor(0f, 0f, 1f)
        val processor = PmVertexProcessor<Vertex, FinalVertex>(program, rawStaticParameters, vertexType) {
            vertex -> FinalVertex(vertex)
        }
        processor.addBuiltinFunction("constructPosition", PmBuiltinFunction(
            listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT), positionType) {
            parameters -> Position(parameters[0].floatValue(), parameters[1].floatValue())
        })

        val model = processor.execute()
        assertEquals(mapOf(Pair("passDistance", PmBuiltinTypes.FLOAT)), model.dynamicParameters)

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
        assertEquals(mapOf(Pair("passDistance", PmBuiltinTypes.FLOAT)), bigMatrix.dynamicParameterTypes)
        assertNull(bigMatrix.propagator)
        assertTrue(bigMatrix.transferredVariables.isEmpty())
        assertEquals(dynamicMatrices[0].instructions, bigMatrix.construction.instructions)

        val smallMatrix = model.matrices[2]!!
        assertEquals(0, smallMatrix.parentIndex)
        assertEquals(mapOf(Pair("passDistance", PmBuiltinTypes.FLOAT)), smallMatrix.dynamicParameterTypes)
        assertNull(smallMatrix.propagator)
        assertTrue(smallMatrix.transferredVariables.isEmpty())
        assertEquals(dynamicMatrices[1].instructions, smallMatrix.construction.instructions)

        val downMatrix = model.matrices[3]!!
        assertEquals(2, downMatrix.parentIndex)
        assertEquals(mapOf(Pair("distance", PmBuiltinTypes.FLOAT)), downMatrix.dynamicParameterTypes)
        assertEquals(childInvocations[1].dynamicParameters.instructions, downMatrix.propagator!!.instructions)
        assertEquals(mapOf(
            Pair("offsetX", Pair(PmBuiltinTypes.FLOAT, PmFloat(0.2f))),
            Pair("offsetY", Pair(PmBuiltinTypes.FLOAT, PmFloat(-0.3f)))
        ), downMatrix.transferredVariables)
        assertEquals(dynamicMatrices[2].instructions, downMatrix.construction.instructions)

        val upMatrix = model.matrices[4]!!
        assertEquals(2, upMatrix.parentIndex)
        assertEquals(mapOf(Pair("distance", PmBuiltinTypes.FLOAT)), upMatrix.dynamicParameterTypes)
        assertEquals(childInvocations[1].dynamicParameters.instructions, upMatrix.propagator!!.instructions)
        assertEquals(mapOf(
            Pair("offsetX", Pair(PmBuiltinTypes.FLOAT, PmFloat(0.2f))),
            Pair("offsetY", Pair(PmBuiltinTypes.FLOAT, PmFloat(-0.3f)))
        ), upMatrix.transferredVariables)
        assertEquals(dynamicMatrices[3].instructions, upMatrix.construction.instructions)

        val translate = PmBuiltinFunction(listOf(
            matrixType, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT
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
            mapOf(Pair("passDistance", PmBuiltinTypes.FLOAT))
        )
        propagationProcessor.execute()
        val propResult = propagationProcessor.result as PmMap
        assertEquals(1, propResult.map.size)
        assertEquals(PmFloat(0.25f), propResult[PmString("distance")])
    }
}
