package procmodel2

import org.joml.Math.toRadians
import procmodel.lang.functions.PmBuiltinFunction
import procmodel.lang.types.PmBuiltinTypes
import procmodel.lang.types.PmNone

object Pm2dBuiltinFunctions {

    private val constructPosition = PmBuiltinFunction(listOf(
        PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT
    ), Pm2dTypes.position) { parameters ->
        Pm2dPositionValue(parameters[0].floatValue(), parameters[1].floatValue())
    }

    private val translate = PmBuiltinFunction(listOf(
        Pm2dTypes.matrix, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT
    ), PmBuiltinTypes.VOID) { parameters ->
        parameters[0].castTo<Pm2dMatrixValue>().matrix.translate(parameters[1].floatValue(), parameters[2].floatValue())
        PmNone()
    }

    private val rotate = PmBuiltinFunction(listOf(
        Pm2dTypes.matrix, PmBuiltinTypes.FLOAT
    ), PmBuiltinTypes.VOID) { parameters ->
        parameters[0].castTo<Pm2dMatrixValue>().matrix.rotate(toRadians(parameters[1].floatValue()))
        PmNone()
    }

    private val scale = PmBuiltinFunction(listOf(
        Pm2dTypes.matrix, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT
    ), PmBuiltinTypes.VOID) { parameters ->
        parameters[0].castTo<Pm2dMatrixValue>().matrix.scale(parameters[1].floatValue(), parameters[2].floatValue())
        PmNone()
    }

    val all = mapOf(
        Pair("constructPosition", constructPosition),
        Pair("translate", translate),
        Pair("rotate", rotate),
        Pair("scale", scale)
    )
}
