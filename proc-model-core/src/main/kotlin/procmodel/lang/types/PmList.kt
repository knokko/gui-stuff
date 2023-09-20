package procmodel.lang.types

import procmodel.exceptions.PmRuntimeError

class PmList(val elements: MutableList<PmValue>): PmValue() {

    override fun toString() = "PmList($elements)"

    override fun copy() = PmList(elements.map { it.copy() }.toMutableList())

    override fun getProperty(propertyName: String): PmValue {
        if (propertyName == "size") return PmInt(elements.size)
        else throw PmRuntimeError("Unknown property: List.$propertyName")
    }

    override operator fun times(right: PmValue): PmValue {
        return if (right is PmInt) {
            val newElements = ArrayList<PmValue>(right.intValue() * elements.size)
            for (counter in 0 until right.intValue()) newElements.addAll(elements)
            PmList(newElements)
        } else super.times(right)
    }

    override operator fun plus(right: PmValue): PmValue {
        return if (right is PmList) PmList((this.elements + right.elements).toMutableList())
        else super.times(right)
    }

    override operator fun get(key: PmValue): PmValue {
        return if (key is PmInt) elements[key.intValue()] else super.get(key)
    }

    override operator fun set(key: PmValue, value: PmValue) {
        if (key is PmInt) elements[key.intValue()] = value
        else super.set(key, value)
    }
}
