package procmodel.lang.types

import kotlin.random.Random

object PmBuiltinTypes {

    val VOID = PmType("void", createDefaultValue = null, valueClass = PmNone::class)

    val ANY = PmType("Any", createDefaultValue = null, valueClass = PmValue::class)

    val FLOAT = PmType("float", createDefaultValue = { PmFloat(0f) }, valueClass = PmFloat::class)

    val INT = PmType("int", createDefaultValue = { PmInt(0) }, valueClass = PmInt::class)

    val STRING = PmType("string", createDefaultValue = null, valueClass = PmString::class)

    val COLOR = PmType("color", createDefaultValue = { PmColor(0f, 0f, 0f) }, valueClass = PmColor::class)

    val MATRIX_INDEX = PmType("matrix", createDefaultValue = { PmMatrixIndex(0) }, valueClass = PmMatrixIndex::class)

    val LIST = PmType("List", createDefaultValue = { PmList(mutableListOf()) }, valueClass = PmList::class)

    val SET = PmType("Set", createDefaultValue = { PmSet(mutableSetOf()) }, valueClass = PmSet::class)

    val MAP = PmType("Map", createDefaultValue = { PmMap() }, valueClass = PmMap::class)

    val RANDOM = PmType("Random", createDefaultValue = { PmRandom(Random.Default) }, valueClass = PmRandom::class)

    val ALL = listOf(VOID, ANY, FLOAT, INT, STRING, COLOR, MATRIX_INDEX, LIST, SET, MAP, RANDOM)
}
