package gruviks.component.slider

import graviks2d.target.GraviksTarget
import graviks2d.util.Color
import gruviks.component.RectangularDrawnRegion
import gruviks.component.RenderResult
import gruviks.event.EventPosition

class CircleSliderStyle(
    val circleColor: Color, val rodColor: Color, val edgeColor: Color = Color.BLACK,
    val rodHeight: Float = 0.15f, val edgeMargin: Float = 0.1f
) : SliderStyle {

    private fun getRadius(aspectRatio: Float): Pair<Float, Float> {
        val radiusY = 0.5f
        val radiusX = radiusY / aspectRatio
        return Pair(radiusX, radiusY)
    }

    override fun getFraction(position: EventPosition, aspectRatio: Float): Float {
        val (radiusX, _) = getRadius(aspectRatio)
        return (position.x - radiusX) / (1f - 2f * radiusX)
    }

    override fun render(target: GraviksTarget, fraction: Float): RenderResult {
        val (radiusX, radiusY) = getRadius(target.getAspectRatio())
        target.fillRoundedRect(
            radiusX, 0.5f - rodHeight * 0.5f,
            1f - radiusX, 0.5f + rodHeight * 0.5f,
            0.5f * rodHeight / target.getAspectRatio(), rodColor
        )

        val centerX = radiusX + (1f - 2f * radiusX) * fraction
        target.fillOval(centerX, 0.5f, radiusX, radiusY, circleColor, edgeMargin)
        target.drawOval(centerX, 0.5f, radiusX, radiusY, edgeColor, edgeMargin, true)

        // Do NOT return a more accurate drawn region because it prevents the slider from receiving cursor events that
        // are right next to the slider
        return RenderResult(
            drawnRegion = RectangularDrawnRegion(0f, 0f, 1f, 1f),
            propagateMissedCursorEvents = false
        )
    }
}
