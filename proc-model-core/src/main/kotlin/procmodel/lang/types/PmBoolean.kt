package procmodel.lang.types

class PmBoolean(
    private val value: Boolean
): PmValue() {
    override fun copy() = this

    override fun booleanValue() = this.value

    override fun toString() = "PmBool($value)"

    override fun equals(other: Any?) = other is PmBoolean && this.value == other.value

    override fun hashCode() = if (value) 1 else 0
}
