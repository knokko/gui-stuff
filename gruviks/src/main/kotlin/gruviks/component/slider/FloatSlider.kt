package gruviks.component.slider

class FloatSlider(
    initialValue: Float, minValue: Float, maxValue: Float, style: SliderStyle, onChange: ((Float) -> Unit)?
): Slider<Float>(initialValue, minValue, maxValue, style, onChange) {

    override fun computeNewValue(newFraction: Float) = minValue + newFraction * (maxValue - minValue)

    override fun computeFraction() = (getValue() - minValue) / (maxValue - minValue)
}
