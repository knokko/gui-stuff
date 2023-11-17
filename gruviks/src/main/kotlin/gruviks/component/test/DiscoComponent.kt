package gruviks.component.test

import graviks2d.target.GraviksTarget
import graviks2d.util.Color
import gruviks.component.Component
import gruviks.component.RectangularDrawnRegion
import gruviks.component.RenderResult
import gruviks.event.CursorClickEvent
import gruviks.event.Event
import gruviks.feedback.RenderFeedback
import org.joml.Math.*
import java.lang.System.currentTimeMillis

class DiscoComponent(
        private var timeOffset: Int = (360_000 * Math.random()).toInt()
): Component() {
    override fun subscribeToEvents() {
        agent.subscribe(CursorClickEvent::class)
    }

    override fun processEvent(event: Event) {
        timeOffset = (360_000 * Math.random()).toInt()
        agent.giveFeedback(RenderFeedback())
    }

    override fun render(target: GraviksTarget, force: Boolean): RenderResult {
        val time = (currentTimeMillis() + timeOffset) % 360_000
        val red = 0.5f + 0.5f * sin(time * 0.000323f)
        val green = 0.5f + 0.5f * cos(time * 0.000967f)
        val blue = 0.5f + 0.5f * sin(red * 2f * PI.toFloat())
        target.fillRect(0f, 0f, 1f, 1f, Color.rgbFloat(red, green, blue))
        agent.giveFeedback(RenderFeedback())
        return RenderResult(
                drawnRegion = RectangularDrawnRegion(0f, 0f, 1f, 1f),
                propagateMissedCursorEvents = true
        )
    }
}
