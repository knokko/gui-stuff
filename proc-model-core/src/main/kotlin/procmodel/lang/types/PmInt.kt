package procmodel.lang.types

import procmodel.exceptions.PmRuntimeError

class PmInt(private val value: Int) : PmValue() {

    override fun intValue() = this.value

    override fun plus(right: PmValue) = if (right is PmInt) PmInt(this.value + right.value) else super.plus(right)

    override fun minus(right: PmValue) = if (right is PmInt) PmInt(this.value - right.value) else super.minus(right)

    override fun times(right: PmValue) = when(right) {
        is PmInt -> PmInt(this.value * right.value)
        is PmFloat -> PmFloat(this.value * right.floatValue())
        is PmList -> right.times(this)
        else -> super.times(right)
    }

    override fun div(right: PmValue) = when(right) {
        is PmInt -> if (right.value != 0) PmInt(this.value / right.value) else throw PmRuntimeError("Division by 0")
        is PmFloat -> PmFloat(this.value / right.floatValue())
        else -> super.div(right)
    }

    override fun compareTo(right: PmValue): Int {
        return if (right is PmInt) this.value.compareTo(right.value)
        else super.compareTo(right)
    }

    override fun equals(other: Any?) = other is PmInt && this.value == other.value

    override fun hashCode() = this.value

    override fun toString() = "PmInt($value)"

    override fun copy() = this
}
