package procmodel.lang.types

class PmFloat(private val value: Float) : PmValue() {

    override fun floatValue() = value

    override fun div(right: PmValue): PmValue {
        return when(right) {
            is PmFloat -> PmFloat(this.value / right.value)
            is PmInt -> PmFloat(this.value / right.intValue())
            else -> super.div(right)
        }
    }

    override fun times(right: PmValue): PmValue {
        return when(right) {
            is PmFloat -> PmFloat(this.value * right.value)
            is PmInt -> PmFloat(this.value * right.intValue())
            else -> super.times(right)
        }
    }

    override fun minus(right: PmValue): PmValue {
        return when(right) {
            is PmFloat -> PmFloat(this.value - right.value)
            is PmInt -> PmFloat(this.value - right.intValue())
            else -> super.minus(right)
        }
    }

    override fun plus(right: PmValue): PmValue {
        return when(right) {
            is PmFloat -> PmFloat(this.value + right.value)
            is PmInt -> PmFloat(this.value + right.intValue())
            else -> super.plus(right)
        }
    }

    override fun toString() = "PmFloat($value)"

    override fun copy() = this

    override fun equals(other: Any?) = other is PmFloat && this.value == other.value

    override fun compareTo(right: PmValue): Int {
        return if (right is PmFloat) value.compareTo(right.value)
        else super.compareTo(right)
    }
    override fun hashCode() = value.hashCode()
}
