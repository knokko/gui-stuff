package procmodel.lang.types

import procmodel.exceptions.PmRuntimeError

class PmSet(val elements: MutableSet<PmValue>): PmValue() {

    override fun toString() = "PmSet($elements)"

    override fun copy() = PmSet(elements.map { it.copy() }.toMutableSet())

    override fun equals(other: Any?) = other is PmSet && this.elements == other.elements

    override fun hashCode() = elements.hashCode()

    override fun getProperty(propertyName: String): PmValue {
        if (propertyName == "size") return PmInt(elements.size)
        else throw PmRuntimeError("Unknown property: List.$propertyName")
    }

    override operator fun plus(right: PmValue): PmValue {
        return if (right is PmSet) PmSet((elements + right.elements).toMutableSet())
        else super.plus(right)
    }
}
