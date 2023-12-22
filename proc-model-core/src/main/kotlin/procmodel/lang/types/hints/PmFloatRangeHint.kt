package procmodel.lang.types.hints

class PmFloatRangeHint(val minValue: Float, val maxValue: Float) : PmTypeHint {

    override fun equals(other: Any?) = other is PmFloatRangeHint && minValue == other.minValue && maxValue == other.maxValue

    override fun hashCode() = minValue.hashCode() - 13 * maxValue.hashCode()

    override fun toString() = "#[range[$minValue, $maxValue]]"
}
