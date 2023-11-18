package procmodel.lang.functions

import org.joml.Math
import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.*

object PmBuiltinFunctions {

    private val PRINT = PmBuiltinFunction(listOf(PmBuiltinTypes.ANY), PmBuiltinTypes.VOID) {
        parameters -> println(parameters[0]); PmNone()
    }
    private val RGB = PmBuiltinFunction(
        listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT), PmBuiltinTypes.COLOR
    ) { parameters ->
        PmColor(parameters[0].floatValue(), parameters[1].floatValue(), parameters[2].floatValue())
    }
    private val FLOAT = PmBuiltinFunction(listOf(PmBuiltinTypes.INT), PmBuiltinTypes.FLOAT) {
        parameters -> PmFloat(parameters[0].intValue().toFloat())
    }
    private val INT = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.INT) {
        parameters -> PmInt(parameters[0].floatValue().toInt())
    }
    private val SIN = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.FLOAT) {
        parameters -> PmFloat(Math.sin(Math.toRadians(parameters[0].floatValue())))
    }
    private val COS = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.FLOAT) {
        parameters -> PmFloat(Math.cos(Math.toRadians(parameters[0].floatValue())))
    }
    private val ADD_TO_COLLECTION = PmBuiltinFunction(listOf(PmBuiltinTypes.ANY, PmBuiltinTypes.ANY), PmBuiltinTypes.ANY) { parameters ->
        val collection = parameters[0]
        if (collection is PmList) collection.elements.add(parameters[1])
        else if (collection is PmSet) collection.elements.add(parameters[1])
        else throw PmRuntimeError("First parameter of add(...) must be a List or Set")

        parameters[0]
    }

    val MAP = mutableMapOf(
        Pair("print", PRINT),
        Pair("rgb", RGB),
        Pair("int", INT),
        Pair("float", FLOAT),
        Pair("sin", SIN),
        Pair("cos", COS),
        Pair("add", ADD_TO_COLLECTION),
    )
}
