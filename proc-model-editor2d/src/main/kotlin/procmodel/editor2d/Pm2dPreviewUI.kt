package procmodel.editor2d

import graviks2d.resource.text.TextStyle
import graviks2d.util.Color
import gruviks.component.Component
import gruviks.component.menu.SimpleFlatMenu
import gruviks.component.menu.controller.SimpleFlatController
import gruviks.component.menu.controller.SimpleListViewController
import gruviks.component.text.TextField
import gruviks.component.text.transparentTextFieldStyle
import gruviks.event.Event
import gruviks.event.UpdateEvent
import gruviks.space.Coordinate
import gruviks.space.Point
import gruviks.space.RectRegion
import gruviks.space.SpaceLayout
import procmodel.lang.types.*

fun createParameterConfigurationMenu(previewComponent: Pm2PreviewComponent): Component {
    val parameterValues = mutableMapOf<String, PmValue>()

    fun updateParameterValues(): String {
        previewComponent.dynamicParameterValues.clear()
        for ((key, value) in parameterValues.entries) {
            previewComponent.dynamicParameterValues[key] = value
        }
        return ""
    }

    val menu = SimpleFlatMenu(SpaceLayout.Simple, backgroundColor)
    menu.addComponent(previewComponent, RectRegion.percentage(0, 0, 70, 100))

    val textFieldStyle = transparentTextFieldStyle(
        defaultStyle = TextStyle(fillColor = Color.rgbInt(200, 200, 200), font = null),
        focusStyle = TextStyle(fillColor = Color.WHITE, font = null)
    )

    val listElements = mutableListOf<Pair<String, PmType>>()
    var lastDynamicParameterTypes = previewComponent.getDynamicParameterTypes()
    for ((name, type) in lastDynamicParameterTypes) {
        listElements.add(Pair(name, type))
        if (type.createDefaultValue != null) {
            parameterValues[name] = type.createDefaultValue!!.invoke()
        }
    }
    updateParameterValues()

    val controller = SimpleListViewController(listElements) { element, _, position, components, _ ->
        val componentPosition = position ?: Point.percentage(70, 100)
        val (name, type) = element
        val component = when (type) {
            PmBuiltinTypes.FLOAT -> TextField.floatField(
                name, parameterValues[name]!!.floatValue(), textFieldStyle,
                onChange = { newValue -> parameterValues[name] = PmFloat(newValue); updateParameterValues() }
            )
            PmBuiltinTypes.INT -> TextField.intField(
                name, parameterValues[name]!!.intValue(), textFieldStyle,
                onChange = { newValue -> parameterValues[name] = PmInt(newValue); updateParameterValues()}
            )
            PmBuiltinTypes.STRING -> TextField(
                name, parameterValues[name]!!.castTo<PmString>().value, textFieldStyle,
                onChange = { newValue -> parameterValues[name] = PmString(newValue); updateParameterValues() }
            )
            else -> return@SimpleListViewController componentPosition
        }

        val region = RectRegion(
            componentPosition.x, componentPosition.y - Coordinate.percentage(10),
            Coordinate.percentage(100), componentPosition.y
        )

        components.add(Pair(component, region))

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
                    for ((name, type) in newDynamicParameterTypes) {
                        listElements.add(Pair(name, type))
                        if (!parameterValues.containsKey(name) && type.createDefaultValue != null) {
                            parameterValues[name] = type.createDefaultValue!!.invoke()
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
