package procmodel.lang.types.hints

class PmIntRangeHint(val minValue: Int, val maxValue: Int) : PmTypeHint {

    override fun equals(other: Any?) = other is PmIntRangeHint && minValue == other.minValue && maxValue == other.maxValue

    override fun hashCode() = minValue - 13 * maxValue

    override fun toString() = "#[range[$minValue, $maxValue]]"
}
