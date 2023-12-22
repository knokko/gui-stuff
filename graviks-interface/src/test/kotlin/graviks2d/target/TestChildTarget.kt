package graviks2d.target

import graviks2d.resource.image.ImageReference
import graviks2d.resource.text.CharacterPosition
import graviks2d.resource.text.FontReference
import graviks2d.resource.text.TextStyle
import graviks2d.util.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

class TestChildTarget {

    @Test
    fun testScissor() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.1f, 0.2f, 0.3f, 0.4f)

        fun assertSimilar(expected: GraviksScissor, actual: GraviksScissor) {
            assertTrue((expected.minX - actual.minX).absoluteValue < 0.001f)
            assertTrue((expected.minY - actual.minY).absoluteValue < 0.001f)
            assertTrue((expected.maxX - actual.maxX).absoluteValue < 0.001f)
            assertTrue((expected.maxY - actual.maxY).absoluteValue < 0.001f)
        }

        child.setScissor(GraviksScissor(0f, 0f, 1f, 1f))
        val defaultScissor = GraviksScissor(0.1f, 0.2f, 0.3f, 0.4f)
        assertSimilar(defaultScissor, parent.getScissor())

        assertSimilar(GraviksScissor(0f, 0f, 1f, 1f), child.setScissor(
            GraviksScissor(0.1f, 0.1f, 0.9f, 0.9f))
        )
        assertSimilar(GraviksScissor(0.12f, 0.22f, 0.28f, 0.38f), parent.currentScissor)
        assertSimilar(GraviksScissor(0.1f, 0.1f, 0.9f, 0.9f), child.getScissor())
    }

    @Test
    fun testFillTriangle() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f
        child.fillTriangle(0.1f, 0.5f, 0.4f, 0.9f, 0f, 1f, Color.BLUE)

        assertEquals(0.24f, parent.x1, margin)
        assertEquals(0.65f, parent.y1, margin)
        assertEquals(0.36f, parent.x2, margin)
        assertEquals(0.77f, parent.y2, margin)
        assertEquals(0.2f, parent.x3, margin)
        assertEquals(0.8f, parent.y3, margin)
    }

    @Test
    fun testFillRect() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f
        child.fillRect(0.1f, 0.5f, 0.4f, 0.9f, Color.BLUE)

        assertEquals(0.24f, parent.x1, margin)
        assertEquals(0.65f, parent.y1, margin)
        assertEquals(0.36f, parent.x2, margin)
        assertEquals(0.77f, parent.y2, margin)
    }

    @Test
    fun testDrawRoundedRect() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f
        child.drawRoundedRect(0.1f, 0.5f, 0.4f, 0.9f, 0.1f, 0.01f, Color.BLUE)

        assertEquals(0.24f, parent.x1, margin)
        assertEquals(0.65f, parent.y1, margin)
        assertEquals(0.36f, parent.x2, margin)
        assertEquals(0.77f, parent.y2, margin)
        assertEquals(0.04f, parent.radiusX, margin)
    }

    @Test
    fun testDrawOval() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f
        child.drawOval(0.1f, 0.5f, 0.1f, 0.2f, Color.RED, 0.2f, true)

        assertEquals(0.2f, parent.x1, margin)
        assertEquals(0.59f, parent.y1, margin)
        assertEquals(0.28f, parent.x2, margin)
        assertEquals(0.71f, parent.y2, margin)
        assertEquals(0.04f, parent.radiusX, margin)
    }

    @Test
    fun testFillOval() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f
        child.fillOval(0.1f, 0.5f, 0.1f, 0.2f, Color.RED, 0f)

        assertEquals(0.2f, parent.x1, margin)
        assertEquals(0.59f, parent.y1, margin)
        assertEquals(0.28f, parent.x2, margin)
        assertEquals(0.71f, parent.y2, margin)
        assertEquals(0.04f, parent.radiusX, margin)
    }

    @Test
    fun testDrawImage() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f
        child.drawImage(0.1f, 0.5f, 0.4f, 0.9f, ImageReference.classLoaderPath("nope", false))

        assertEquals(0.24f, parent.x1, margin)
        assertEquals(0.65f, parent.y1, margin)
        assertEquals(0.36f, parent.x2, margin)
        assertEquals(0.77f, parent.y2, margin)
    }

    @Test
    fun testGetImageSize() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        assertEquals(Pair(16, 64), child.getImageSize(ImageReference.classLoaderPath("test", false)))
    }

    @Test
    fun testDrawString() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.2f, 0.5f, 0.6f, 0.8f)
        val margin = 0.0001f

        val result = child.drawString(
            0.1f, 0.2f, 0.5f, 0.8f, "T", TextStyle(
                fillColor = Color.GREEN, font = null
            )
        )

        assertEquals(0.24f, parent.x1, margin)
        assertEquals(0.56f, parent.y1, margin)
        assertEquals(0.4f, parent.x2, margin)
        assertEquals(0.74f, parent.y2, margin)

        assertEquals(1, result.size)
        assertEquals(0.15f, result[0].minX, margin)
        assertEquals(1f / 3f, result[0].minY, margin)
        assertEquals(0.375f, result[0].maxX, margin)
        assertEquals(2f / 3f, result[0].maxY, margin)
    }

    @Test
    fun testGetSize() {
        val parent = LoggedTarget(400, 800)

        val child = ChildTarget(parent, 0.2f, 0.5f, 0.7f, 0.75f)
        val (width, height) = child.getSize()
        assertEquals(200, width)
        assertEquals(200, height)
    }

    @Test
    fun testGetStringAspectRatio() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.1f, 0.3f, 0.3f, 0.4f)

        assertEquals(5f, child.getStringAspectRatio("test", null), 0.01f)
    }

    @Test
    fun testGetAspectRatio() {
        val parent = LoggedTarget()
        val child = ChildTarget(parent, 0.1f, 0.3f, 0.3f, 0.4f)

        assertEquals(3f, child.getAspectRatio(), 0.01f)
    }
}

private class LoggedTarget(val width: Int = 1500, val height: Int = 1000) : GraviksTarget {

    var x1 = 0f
    var y1 = 0f
    var x2 = 0f
    var y2 = 0f
    var x3 = 0f
    var y3 = 0f
    var radiusX = 0f
    var currentScissor = GraviksScissor(0f, 0f, 1f, 1f)

    override fun setScissor(newScissor: GraviksScissor): GraviksScissor {
        val oldScissor = this.currentScissor
        this.currentScissor = newScissor
        return oldScissor
    }

    override fun getScissor() = this.currentScissor

    override fun getSize() = Pair(width, height)

    override fun fillTriangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, color: Color) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
        this.x3 = x3
        this.y3 = y3
    }

    override fun fillRect(x1: Float, y1: Float, x2: Float, y2: Float, color: Color) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
    }

    override fun drawRect(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: Color) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
    }

    override fun drawRoundedRect(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        radiusX: Float,
        lineWidth: Float,
        color: Color
    ) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
        this.radiusX = radiusX
    }

    override fun drawOval(
        centerX: Float,
        centerY: Float,
        radiusX: Float,
        radiusY: Float,
        color: Color,
        edgeMargin: Float,
        antiAlias: Boolean
    ) {
        this.x1 = centerX - radiusX
        this.y1 = centerY - radiusY
        this.x2 = centerX + radiusX
        this.y2 = centerY + radiusY
        this.radiusX = radiusX
    }

    override fun fillOval(centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, color: Color, edgeMargin: Float) {
        this.x1 = centerX - radiusX
        this.y1 = centerY - radiusY
        this.x2 = centerX + radiusX
        this.y2 = centerY + radiusY
        this.radiusX = radiusX
    }

    override fun drawImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, image: ImageReference) {
        this.x1 = xLeft
        this.y1 = yBottom
        this.x2 = xRight
        this.y2 = yTop
    }

    override fun getImageSize(image: ImageReference) = Pair(16, 64)

    override fun drawString(
        minX: Float,
        yBottom: Float,
        maxX: Float,
        yTop: Float,
        string: String,
        style: TextStyle,
        dryRun: Boolean,
        suggestLeftToRight: Boolean
    ): List<CharacterPosition> {
        this.x1 = minX
        this.y1 = yBottom
        this.x2 = maxX
        this.y2 = yTop
        return listOf(CharacterPosition(0.26f, 0.6f, 0.35f, 0.7f, isLeftToRight = true))
    }

    override fun getStringAspectRatio(string: String, fontReference: FontReference?) = 5f

    override fun getAspectRatio() = 1.5f
}
