package gruviks.component.slider

import kotlin.math.roundToInt

class IntSlider(
    initialValue: Int, minValue: Int, maxValue: Int, style: SliderStyle, onChange: ((Int) -> Unit)?
) : Slider<Int>(initialValue, minValue, maxValue, style, onChange) {

    override fun computeNewValue(newFraction: Float) = (minValue + newFraction * (maxValue - minValue)).roundToInt()

    override fun computeFraction() = (getValue() - minValue).toFloat() / (maxValue - minValue)
}
