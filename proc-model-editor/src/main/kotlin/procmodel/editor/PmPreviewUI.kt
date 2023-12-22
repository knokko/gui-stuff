package procmodel.editor

import graviks2d.resource.text.TextAlignment
import graviks2d.resource.text.TextStyle
import graviks2d.util.Color
import gruviks.component.Component
import gruviks.component.HorizontalComponentAlignment
import gruviks.component.menu.SimpleFlatMenu
import gruviks.component.menu.controller.SimpleFlatController
import gruviks.component.menu.controller.SimpleListViewController
import gruviks.component.slider.CircleSliderStyle
import gruviks.component.slider.FloatSlider
import gruviks.component.slider.IntSlider
import gruviks.component.text.TextComponent
import gruviks.component.text.TextField
import gruviks.component.text.transparentTextFieldStyle
import gruviks.event.Event
import gruviks.event.UpdateEvent
import gruviks.space.Coordinate
import gruviks.space.Point
import gruviks.space.RectRegion
import gruviks.space.SpaceLayout
import org.joml.Math.clamp
import procmodel.lang.types.*
import procmodel.lang.types.hints.PmFloatRangeHint
import procmodel.lang.types.hints.PmIntRangeHint
import procmodel.model.PmModel

interface PmPreviewComponent<Vertex> {

    val dynamicParameterValues: MutableMap<String, PmValue>

    fun getDynamicParameterTypes(): Map<String, PmFatType>

    fun updateModel(newModel: PmModel<Vertex>)
}

fun <Vertex> createParameterConfigurationMenu(previewComponent: PmPreviewComponent<Vertex>): Component {
    val parameterValues = mutableMapOf<String, PmValue>()

    fun updateParameterValues(): String {
        previewComponent.dynamicParameterValues.clear()
        for ((key, value) in parameterValues.entries) {
            previewComponent.dynamicParameterValues[key] = value
        }
        return ""
    }

    val menu = SimpleFlatMenu(SpaceLayout.Simple, backgroundColor)
    menu.addComponent(previewComponent as Component, RectRegion.percentage(0, 0, 70, 100))

    val textFieldStyle = transparentTextFieldStyle(
        defaultStyle = TextStyle(fillColor = Color.rgbInt(200, 200, 200), font = null),
        focusStyle = TextStyle(fillColor = Color.WHITE, font = null)
    )
    val sliderTextStyle = TextStyle(
        fillColor = Color.rgbInt(200, 200, 200),
        font = null, alignment = TextAlignment.Centered
    )
    val sliderStyle = CircleSliderStyle(
        circleColor = Color.rgbInt(0, 150, 220),
        rodColor = Color.rgbInt(0, 60, 100)
    )

    val listElements = mutableListOf<Pair<String, PmFatType>>()
    var lastDynamicParameterTypes = previewComponent.getDynamicParameterTypes()
    for ((name, fatType) in lastDynamicParameterTypes) {
        listElements.add(Pair(name, fatType))
        if (fatType.hint != null) {
            if (fatType.hint is PmFloatRangeHint) {
                val range = fatType.hint as PmFloatRangeHint
                parameterValues[name] = PmFloat(clamp(range.minValue, range.maxValue, 0f))
            }
            if (fatType.hint is PmIntRangeHint) {
                val range = fatType.hint as PmIntRangeHint
                parameterValues[name] = PmInt(clamp(range.minValue, range.maxValue, 0))
            }
        } else if (fatType.type.createDefaultValue != null) {
            parameterValues[name] = fatType.type.createDefaultValue!!.invoke()
        }
    }
    updateParameterValues()

    val controller = SimpleListViewController(listElements) { element, _, position, components, _ ->
        val componentPosition = position ?: Point.percentage(70, 100)
        val region = RectRegion(
            componentPosition.x, componentPosition.y - Coordinate.percentage(10),
            Coordinate.percentage(100), componentPosition.y
        )

        val (name, type) = element

        if (type.hint == null) {
            val component = when (type.type) {
                PmBuiltinTypes.FLOAT -> TextField.floatField(
                    name, parameterValues[name]!!.floatValue(), textFieldStyle,
                    onChange = { newValue -> parameterValues[name] = PmFloat(newValue); updateParameterValues() }
                )

                PmBuiltinTypes.INT -> TextField.intField(
                    name, parameterValues[name]!!.intValue(), textFieldStyle,
                    onChange = { newValue -> parameterValues[name] = PmInt(newValue); updateParameterValues() }
                )

                PmBuiltinTypes.STRING -> TextField(
                    name, parameterValues[name]!!.castTo<PmString>().value, textFieldStyle,
                    onChange = { newValue -> parameterValues[name] = PmString(newValue); updateParameterValues() }
                )

                else -> return@SimpleListViewController componentPosition
            }

            components.add(Pair(component, region))
        }

        if (type.hint is PmFloatRangeHint || type.hint is PmIntRangeHint) {
            val midY = (region.minY + region.boundY) / 2
            val textRegion = RectRegion(region.minX, midY, region.boundX, region.boundY)
            val sliderRegion = RectRegion(region.minX, region.minY, region.boundX, midY)

            if (type.hint is PmFloatRangeHint) {
                val rangeHint = type.hint as PmFloatRangeHint
                components.add(Pair(TextComponent("${rangeHint.minValue} $name ${rangeHint.maxValue}", sliderTextStyle), textRegion))
                components.add(Pair(FloatSlider(
                    parameterValues[name]!!.floatValue(), rangeHint.minValue, rangeHint.maxValue, sliderStyle, onChange = { newValue ->
                        parameterValues[name] = PmFloat(newValue)
                        updateParameterValues()
                    }
                ), sliderRegion))
            }
            if (type.hint is PmIntRangeHint) {
                val rangeHint = type.hint as PmIntRangeHint
                components.add(Pair(TextComponent("${rangeHint.minValue} $name ${rangeHint.maxValue}", sliderTextStyle), textRegion))
                components.add(Pair(IntSlider(
                    parameterValues[name]!!.intValue(), rangeHint.minValue, rangeHint.maxValue, sliderStyle, onChange = { newValue ->
                        parameterValues[name] = PmInt(newValue)
                        updateParameterValues()
                    }
                ), sliderRegion))
            }
        }

        Point(componentPosition.x, componentPosition.y - Coordinate.percentage(11))
    }
    menu.addController(controller)

    class RefreshController : SimpleFlatController() {
        override fun processEvent(event: Event) {
            if (event is UpdateEvent) {
                val newDynamicParameterTypes = previewComponent.getDynamicParameterTypes()
                if (newDynamicParameterTypes != lastDynamicParameterTypes) {
                    lastDynamicParameterTypes = newDynamicParameterTypes
                    listElements.clear()
                    for ((name, fatType) in newDynamicParameterTypes) {
                        listElements.add(Pair(name, fatType))
                        if (!parameterValues.containsKey(name) && fatType.type.createDefaultValue != null) {
                            parameterValues[name] = fatType.type.createDefaultValue!!.invoke()
                        }
                    }
                    parameterValues.keys.removeIf { !newDynamicParameterTypes.containsKey(it) }
                    updateParameterValues()
                    controller.refresh()
                }
            }
        }
    }
    menu.addController(RefreshController())

    return menu
}
