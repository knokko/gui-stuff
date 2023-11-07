package procmodel.lang.types

import kotlin.reflect.KClass

class PmType(
    val name: String,
    val createDefaultValue: (() -> PmValue)?,
    val valueClass: KClass<*>
) {
    fun acceptValue(value: PmValue) = valueClass.isInstance(value)

    override fun toString() = name
}
