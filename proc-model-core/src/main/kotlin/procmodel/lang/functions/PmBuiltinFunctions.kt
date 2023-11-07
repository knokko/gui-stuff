package procmodel.lang.functions

import org.joml.Math
import procmodel.lang.types.*

object PmBuiltinFunctions {

    val PRINT = PmBuiltinFunction(listOf(PmBuiltinTypes.ANY), PmBuiltinTypes.VOID) {
        parameters -> println(parameters[0]); PmNone()
    }
    val RGB = PmBuiltinFunction(
        listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT), PmBuiltinTypes.COLOR
    ) { parameters ->
        PmColor(parameters[0].floatValue(), parameters[1].floatValue(), parameters[2].floatValue())
    }
    val FLOAT = PmBuiltinFunction(listOf(PmBuiltinTypes.INT), PmBuiltinTypes.FLOAT) {
        parameters -> PmFloat(parameters[0].intValue().toFloat())
    }
    val INT = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.INT) {
        parameters -> PmInt(parameters[0].floatValue().toInt())
    }
    val SIN = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.FLOAT) {
        parameters -> PmFloat(Math.sin(Math.toRadians(parameters[0].floatValue())))
    }
    val COS = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.FLOAT) {
        parameters -> PmFloat(Math.cos(Math.toRadians(parameters[0].floatValue())))
    }
    val ADD_TO_LIST = PmBuiltinFunction(listOf(PmBuiltinTypes.LIST, PmBuiltinTypes.ANY), PmBuiltinTypes.LIST) { parameters ->
        parameters[0].castTo<PmList>().elements.add(parameters[1])
        parameters[0]
    }

    val MAP = mutableMapOf(
        Pair("print", PRINT),
        Pair("rgb", RGB),
        Pair("int", INT),
        Pair("float", FLOAT),
        Pair("sin", SIN),
        Pair("cos", COS),
        Pair("add", ADD_TO_LIST),
    )
}
