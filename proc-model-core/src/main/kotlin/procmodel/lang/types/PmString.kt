package procmodel.lang.types

class PmString(val value: String): PmValue() {

    override fun copy() = this

    override fun compareTo(right: PmValue): Int {
        return if (right is PmString) this.value.compareTo(right.value)
        else super.compareTo(right)
    }

    override fun equals(other: Any?) = other is PmString && this.value == other.value

    override fun hashCode() = this.value.hashCode()

    override fun toString() = "PmString($value)"
}
