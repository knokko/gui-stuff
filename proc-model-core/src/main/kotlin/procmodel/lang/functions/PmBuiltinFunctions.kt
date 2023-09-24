package procmodel.lang.functions

import procmodel.lang.types.PmBuiltinTypes

object PmBuiltinFunctions {

    val PRINT = PmBuiltinFunction(listOf(PmBuiltinTypes.ANY), PmBuiltinTypes.VOID)
    val OUTPUT_VALUE = PmBuiltinFunction(listOf(PmBuiltinTypes.ANY), PmBuiltinTypes.VOID)
    val PRODUCE_TRIANGLE = PmBuiltinFunction(listOf(PmBuiltinTypes.ANY, PmBuiltinTypes.ANY, PmBuiltinTypes.ANY), PmBuiltinTypes.VOID)
    val RGB = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT, PmBuiltinTypes.FLOAT), PmBuiltinTypes.COLOR)
    val FLOAT = PmBuiltinFunction(listOf(PmBuiltinTypes.INT), PmBuiltinTypes.FLOAT)
    val INT = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.INT)
    val SIN = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.FLOAT)
    val COS = PmBuiltinFunction(listOf(PmBuiltinTypes.FLOAT), PmBuiltinTypes.FLOAT)
    val ADD_TO_LIST = PmBuiltinFunction(listOf(PmBuiltinTypes.LIST, PmBuiltinTypes.ANY), PmBuiltinTypes.LIST)

    val MAP = mutableMapOf(
        Pair("print", PRINT),
        Pair("outputValue", OUTPUT_VALUE),
        Pair("produceTriangle", PRODUCE_TRIANGLE),
        Pair("rgb", RGB),
        Pair("int", INT),
        Pair("float", FLOAT),
        Pair("sin", SIN),
        Pair("cos", COS),
        Pair("add", ADD_TO_LIST),
    )
}
