package procmodel.test

import graviks2d.util.Color
import org.joml.Matrix3x2f
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.types.*
import procmodel.lang.types.hints.PmFloatRangeHint
import procmodel.program.*

object PmTestCase {
    val vertexType = PmType("Vertex", { Vertex(null, PmColor(Color.BLACK), PmMatrixIndex(0)) }, Vertex::class)
    val matrixType = PmType("Matrix", { Matrix(Matrix3x2f()) }, Matrix::class)
    val positionType = PmType("position", null, Position::class)

    private val mainBody = listOf(
        PmInstruction.pushValue(PmMap(), 8), // index 0
        PmInstruction.declareVariable("triangleParameters", PmBuiltinTypes.MAP, 8), // index 1
        PmInstruction.pushVariable("triangleParameters", 9), // index 2
        PmInstruction.pushValue(PmString("color"), 9), // index 3
        PmInstruction.pushVariable("ownColor", 9), // index 4
        PmInstruction.writeIndexed(9), // index 5
        PmInstruction.pushValue(PmInt(0), 11), // index 6
        PmInstruction.createDynamicMatrix(11), // index 7
        PmInstruction.declareVariable("bigMatrix", PmBuiltinTypes.MATRIX_INDEX, 11), // index 8
        PmInstruction.pushVariable("bigMatrix", 17), // index 9
        PmInstruction.pushVariable("triangleParameters", 17), // index 10
        PmInstruction.pushValue(PmInt(0), 17), // index 11
        PmInstruction.createChildModel("/minimal/triangle.pm2", 17), // index 12
        PmInstruction.pushValue(PmMap(), 19), // index 13
        PmInstruction.declareVariable("nestedParameters", PmBuiltinTypes.MAP, 19), // index 14
        PmInstruction.pushVariable("nestedParameters", 20), // index 15
        PmInstruction.pushValue(PmString("offsetX"), 20), // index 16
        PmInstruction.pushValue(PmFloat(0.2f), 20), // index 17
        PmInstruction.writeIndexed(20), // index 18
        PmInstruction.pushVariable("nestedParameters", 21), // index 19
        PmInstruction.pushValue(PmString("offsetY"), 21), // index 20
        PmInstruction.pushValue(PmFloat(-0.3f), 21), // index 21
        PmInstruction.writeIndexed(21), // index 22
        PmInstruction.pushVariable("nestedParameters", 22), // index 23
        PmInstruction.pushValue(PmString("color1"), 22), // index 24
        PmInstruction.pushValue(PmFloat(1.0f), 22), // index 25
        PmInstruction.pushValue(PmFloat(0.0f), 22), // index 26
        PmInstruction.pushValue(PmFloat(0.0f), 22), // index 27
        PmInstruction.invokeBuiltinFunction("rgb", 22), // index 28
        PmInstruction.writeIndexed(22), // index 29
        PmInstruction.pushVariable("nestedParameters", 23), // index 30
        PmInstruction.pushValue(PmString("color2"), 23), // index 31
        PmInstruction.pushValue(PmFloat(0.0f), 23), // index 32
        PmInstruction.pushValue(PmFloat(1.0f), 23), // index 33
        PmInstruction.pushValue(PmFloat(0.0f), 23), // index 34
        PmInstruction.invokeBuiltinFunction("rgb", 23), // index 35
        PmInstruction.writeIndexed(23), // index 36
        PmInstruction.pushValue(PmInt(1), 25), // index 37
        PmInstruction.createDynamicMatrix(25), // index 38
        PmInstruction.declareVariable("smallMatrix", PmBuiltinTypes.MATRIX_INDEX, 25), // index 39
        PmInstruction.pushVariable("smallMatrix", 30), // index 40
        PmInstruction.pushVariable("nestedParameters", 30), // index 41
        PmInstruction.pushValue(PmInt(1), 30), // index 42
        PmInstruction.createChildModel("/minimal/nested.pm2", 30), // index 43
        PmInstruction.exitProgram(35), // index 44
    )
    private val nestedBody = listOf(
        PmInstruction.pushValue(PmMap(), 10), // index 44
        PmInstruction.declareVariable("childParameters1", PmBuiltinTypes.MAP, 10), // index 45
        PmInstruction.pushVariable("childParameters1", 11), // index 46
        PmInstruction.pushValue(PmString("color"), 11), // index 47
        PmInstruction.pushVariable("color1", 11), // index 48
        PmInstruction.writeIndexed(11), // index 49
        PmInstruction.pushValue(PmMap(), 13), // index 50
        PmInstruction.declareVariable("childParameters2", PmBuiltinTypes.MAP, 13), // index 51
        PmInstruction.pushVariable("childParameters2", 14), // index 52
        PmInstruction.pushValue(PmString("color"), 14), // index 53
        PmInstruction.pushVariable("color2", 14), // index 54
        PmInstruction.writeIndexed(14), // index 55
        PmInstruction.transferVariable("offsetX", PmBuiltinTypes.FLOAT, 16), // index 56
        PmInstruction.transferVariable("offsetY", PmBuiltinTypes.FLOAT, 16), // index 57
        PmInstruction.pushValue(PmInt(2), 16), // index 58
        PmInstruction.createDynamicMatrix(16), // index 59
        PmInstruction.declareVariable("downMatrix", PmBuiltinTypes.MATRIX_INDEX, 16), // index 60
        PmInstruction.transferVariable("offsetX", PmBuiltinTypes.FLOAT, 21), // index 61
        PmInstruction.transferVariable("offsetY", PmBuiltinTypes.FLOAT, 21), // index 62
        PmInstruction.pushValue(PmInt(3), 21), // index 63
        PmInstruction.createDynamicMatrix(21), // index 64
        PmInstruction.declareVariable("upMatrix", PmBuiltinTypes.MATRIX_INDEX, 21), // index 65
        PmInstruction.pushVariable("downMatrix", 26), // index 66
        PmInstruction.pushVariable("childParameters1", 26), // index 67
        PmInstruction.pushValue(PmInt(2), 26), // index 68
        PmInstruction.createChildModel("/minimal/triangle.pm2", 26), // index 69
        PmInstruction.pushVariable("upMatrix", 27), // index 70
        PmInstruction.pushVariable("childParameters2", 27), // index 71
        PmInstruction.pushValue(PmInt(3), 27), // index 72
        PmInstruction.createChildModel("/minimal/triangle.pm2", 27), // index 73
        PmInstruction.exitProgram(28) // index 74
    )
    private val triangleBody = listOf(
        PmInstruction.pushValue(Vertex(null, PmColor(0.0f, 0.0f, 0.0f), PmMatrixIndex(0)), 3), // index 75
        PmInstruction.declareVariable("bottomLeft", vertexType, 3), // index 76
        PmInstruction.pushVariable("bottomLeft", 4), // index 77
        PmInstruction.pushValue(PmFloat(-1.0f), 4), // index 78
        PmInstruction.pushValue(PmFloat(-1.0f), 4), // index 79
        PmInstruction.invokeBuiltinFunction("constructPosition", 4), // index 80
        PmInstruction.setProperty("position", 4), // index 81
        PmInstruction.pushVariable("bottomLeft", 5), // index 82
        PmInstruction.pushVariable("color", 5), // index 83
        PmInstruction.setProperty("color", 5), // index 84
        PmInstruction.pushValue(Vertex(null, PmColor(0.0f, 0.0f, 0.0f), PmMatrixIndex(0)), 7), // index 85
        PmInstruction.declareVariable("bottomRight", vertexType, 7), // index 86
        PmInstruction.pushVariable("bottomRight", 8), // index 87
        PmInstruction.pushValue(PmFloat(1.0f), 8), // index 88
        PmInstruction.pushValue(PmFloat(-1.0f), 8), // index 89
        PmInstruction.invokeBuiltinFunction("constructPosition", 8), // index 90
        PmInstruction.setProperty("position", 8), // index 91
        PmInstruction.pushVariable("bottomRight", 9), // index 92
        PmInstruction.pushVariable("color", 9), // index 93
        PmInstruction.setProperty("color", 9), // index 94
        PmInstruction.pushValue(Vertex(null, PmColor(0.0f, 0.0f, 0.0f), PmMatrixIndex(0)), 11), // index 95
        PmInstruction.declareVariable("top", vertexType, 11), // index 96
        PmInstruction.pushVariable("top", 12), // index 97
        PmInstruction.pushValue(PmFloat(0.0f), 12), // index 98
        PmInstruction.pushValue(PmFloat(1.0f), 12), // index 99
        PmInstruction.invokeBuiltinFunction("constructPosition", 12), // index 100
        PmInstruction.setProperty("position", 12), // index 101
        PmInstruction.pushVariable("top", 13), // index 102
        PmInstruction.pushVariable("color", 13), // index 103
        PmInstruction.setProperty("color", 13), // index 104
        PmInstruction.pushVariable("bottomLeft", 15), // index 105
        PmInstruction.pushVariable("bottomRight", 15), // index 106
        PmInstruction.pushVariable("top", 15), // index 107
        PmInstruction.invokeBuiltinFunction("produceTriangle", 15), // index 108
        PmInstruction.delete(15), // index 109
        PmInstruction.exitProgram(16), // index 110
    )
    val dynamicMatrices = listOf(
        PmDynamicMatrixConstructor(listOf(
            PmInstruction.pushValue(Matrix(Matrix3x2f()), 12),
            PmInstruction.declareVariable("bigMatrix", matrixType, 12),
            PmInstruction.pushVariable("bigMatrix", 13),
            PmInstruction.pushValue(PmFloat(4.0f), 13),
            PmInstruction.pushValue(PmFloat(4.0f), 13),
            PmInstruction.invokeBuiltinFunction("scale", 13),
            PmInstruction.delete(13),
            PmInstruction.pushVariable("bigMatrix", 14),
            PmInstruction.invokeBuiltinFunction("outputValue", 14),
            PmInstruction.delete(14),
            PmInstruction.exitProgram(15),
        )),
        PmDynamicMatrixConstructor(listOf(
            PmInstruction.pushValue(Matrix(Matrix3x2f()), 26),
            PmInstruction.declareVariable("smallMatrix", matrixType, 26),
            PmInstruction.pushVariable("smallMatrix", 27),
            PmInstruction.pushValue(PmFloat(0.4f), 27),
            PmInstruction.pushValue(PmFloat(0.4f), 27),
            PmInstruction.invokeBuiltinFunction("scale", 27),
            PmInstruction.delete(27),
            PmInstruction.pushVariable("smallMatrix", 28),
            PmInstruction.invokeBuiltinFunction("outputValue", 28),
            PmInstruction.delete(28),
            PmInstruction.exitProgram(29)
        )),
        PmDynamicMatrixConstructor(listOf(
            PmInstruction.pushValue(Matrix(Matrix3x2f()), 17),
            PmInstruction.declareVariable("downMatrix", matrixType, 17),
            PmInstruction.pushVariable("downMatrix", 18),
            PmInstruction.pushVariable("offsetX", 18),
            PmInstruction.pushVariable("offsetY", 18),
            PmInstruction.pushVariable("distance", 18),
            PmInstruction.subtract(18),
            PmInstruction.invokeBuiltinFunction("translate", 18),
            PmInstruction.delete(18),
            PmInstruction.pushVariable("downMatrix", 19),
            PmInstruction.invokeBuiltinFunction("outputValue", 19),
            PmInstruction.delete(19),
            PmInstruction.exitProgram(20)
        )),
        PmDynamicMatrixConstructor(listOf(
            PmInstruction.pushValue(Matrix(Matrix3x2f()), 22),
            PmInstruction.declareVariable("upMatrix", matrixType, 22),
            PmInstruction.pushVariable("upMatrix", 23),
            PmInstruction.pushVariable("offsetX", 23),
            PmInstruction.pushVariable("offsetY", 23),
            PmInstruction.pushVariable("distance", 23),
            PmInstruction.add(23),
            PmInstruction.invokeBuiltinFunction("translate", 23),
            PmInstruction.delete(23),
            PmInstruction.pushVariable("upMatrix", 24),
            PmInstruction.invokeBuiltinFunction("outputValue", 24),
            PmInstruction.delete(24),
            PmInstruction.exitProgram(25)
        ))
    )
    val childInvocations = listOf(
        PmParameterPropagator(listOf(
            PmInstruction.pushValue(PmMap(), 17),
            PmInstruction.invokeBuiltinFunction("outputValue", 17),
            PmInstruction.delete(17),
            PmInstruction.exitProgram(17)
        )),
        PmParameterPropagator(listOf(
            PmInstruction.pushValue(PmMap(), 31),
            PmInstruction.declareVariable("parameters", PmBuiltinTypes.MAP, 31),
            PmInstruction.pushVariable("parameters", 32),
            PmInstruction.pushValue(PmString("distance"), 32),
            PmInstruction.pushVariable("passDistance", 32),
            PmInstruction.writeIndexed(32),
            PmInstruction.pushVariable("parameters", 33),
            PmInstruction.invokeBuiltinFunction("outputValue", 33),
            PmInstruction.delete(33),
            PmInstruction.exitProgram(34)
        )),
        PmParameterPropagator(listOf(
            PmInstruction.pushValue(PmMap(), 26),
            PmInstruction.invokeBuiltinFunction("outputValue", 26),
            PmInstruction.delete(26),
            PmInstruction.exitProgram(26)
        )),
        PmParameterPropagator(listOf(
            PmInstruction.pushValue(PmMap(), 27),
            PmInstruction.invokeBuiltinFunction("outputValue", 27),
            PmInstruction.delete(27),
            PmInstruction.exitProgram(27)
        ))
    )
    private val children = mapOf(
        Pair("/minimal/triangle.pm2", PmChildProgram(
            mainBody.size + nestedBody.size, mapOf(Pair("color", PmBuiltinTypes.COLOR)), emptyMap()
        )),
        Pair("/minimal/nested.pm2", PmChildProgram(mainBody.size, mapOf(
            Pair("offsetX", PmBuiltinTypes.FLOAT),
            Pair("offsetY", PmBuiltinTypes.FLOAT),
            Pair("color1", PmBuiltinTypes.COLOR),
            Pair("color2", PmBuiltinTypes.COLOR)
        ), mapOf(Pair("distance", PmFatType(PmBuiltinTypes.FLOAT, null)))))
    )
    private val staticParameterTypes = mapOf(Pair("ownColor", PmBuiltinTypes.COLOR))
    private val dynamicParameterTypes = mapOf(Pair("passDistance", PmFatType(PmBuiltinTypes.FLOAT, PmFloatRangeHint(0f, 1f))))
    val program = PmProgram(
        PmProgramBody(mainBody + nestedBody + triangleBody),
        dynamicMatrices,
        childInvocations,
        children,
        staticParameterTypes,
        dynamicParameterTypes
    )
}
