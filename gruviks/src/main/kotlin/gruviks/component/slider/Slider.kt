package gruviks.component.slider

import graviks2d.target.GraviksTarget
import gruviks.component.Component
import gruviks.component.RenderResult
import gruviks.event.*
import gruviks.feedback.RenderFeedback

abstract class Slider<T: Comparable<T>>(
    initialValue: T,
    val minValue: T,
    val maxValue: T,
    val style: SliderStyle,
    val onChange: ((T) -> Unit)?
) : Component() {

    private var currentValue = clamp(initialValue)
    private var lastAspectRatio = 0f

    private fun clamp(value: T): T {
        if (value < minValue) return minValue
        if (value > maxValue) return maxValue
        return value
    }

    protected abstract fun computeNewValue(newFraction: Float): T

    protected abstract fun computeFraction(): Float

    fun getValue() = currentValue

    fun setValue(newValue: T) {
        val clampedValue = clamp(newValue)
        if (clampedValue != currentValue) {
            currentValue = clampedValue
            agent.giveFeedback(RenderFeedback())
            onChange?.invoke(clampedValue)
        }
    }

    override fun subscribeToEvents() {
        agent.subscribe(CursorMoveEvent::class)
    }

    override fun processEvent(event: Event) {
        if (event is CursorMoveEvent && lastAspectRatio != 0f) {
            val cursorState = agent.cursorTracker.getCursorState(event.cursor)
            if (cursorState != null && cursorState.pressedButtons.isNotEmpty()) {
                val newFraction = style.getFraction(event.newPosition, lastAspectRatio)
                setValue(computeNewValue(newFraction))
            }
        } else throw IllegalArgumentException("Unexpected event $event")
    }

    override fun render(target: GraviksTarget, force: Boolean): RenderResult {
        lastAspectRatio = target.getAspectRatio()
        return style.render(target, computeFraction())
    }
}
