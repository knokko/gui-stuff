package gruviks.component.slider

import graviks2d.target.GraviksTarget
import gruviks.component.RenderResult
import gruviks.event.EventPosition

interface SliderStyle {

    fun getFraction(position: EventPosition, aspectRatio: Float): Float

    fun render(target: GraviksTarget, fraction: Float): RenderResult
}
