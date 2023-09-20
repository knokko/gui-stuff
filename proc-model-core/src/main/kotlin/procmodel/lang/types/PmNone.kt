package procmodel.lang.types

class PmNone : PmValue() {
    override fun toString() = "NoneValue"

    override fun copy() = this

    override fun equals(other: Any?) = other is PmNone

    override fun hashCode() = 123456
}
