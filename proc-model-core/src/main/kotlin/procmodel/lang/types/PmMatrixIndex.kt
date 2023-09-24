package procmodel.lang.types

class PmMatrixIndex(val index: Int) : PmValue() {
    override fun copy() = this

    override fun equals(other: Any?) = other is PmMatrixIndex && this.index == other.index

    override fun hashCode() = this.index

    override fun toString() = "PmMatrixIndex($index)"
}
