package graviks2d.target

import graviks2d.resource.image.ImageReference
import graviks2d.resource.text.CharacterPosition
import graviks2d.resource.text.FontReference
import graviks2d.resource.text.TextStyle
import graviks2d.util.Color

interface GraviksTarget {
    fun setScissor(newScissor: GraviksScissor): GraviksScissor

    fun getScissor(): GraviksScissor

    /**
     * Gets the size of this render target, in pixels. The first element of the returned `Pair` is the width and the
     * second element is the height.
     */
    fun getSize(): Pair<Int, Int>

    fun fillTriangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, color: Color)

    fun fillRect(x1: Float, y1: Float, x2: Float, y2: Float, color: Color)

    fun drawRect(x1: Float, y1: Float, x2: Float, y2: Float, lineWidth: Float, color: Color)

    fun drawRoundedRect(
        x1: Float, y1: Float, x2: Float, y2: Float, radiusX: Float, lineWidth: Float, color: Color
    )

    fun fillRoundedRect(
        x1: Float, y1: Float, x2: Float, y2: Float, radiusX: Float, color: Color
    ) {
        drawRoundedRect(x1, y1, x2, y2, radiusX, 0f, color)
    }

    fun fillOval(
        centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, color: Color, edgeMargin: Float
    )

    fun drawImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, image: ImageReference)

    fun getImageSize(image: ImageReference): Pair<Int, Int>

    @Throws(UnsupportedOperationException::class)
    fun drawVulkanImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, vkImage: Long, vkImageView: Long) {
        throw UnsupportedOperationException("This implementation (${this::class.java.simpleName}) doesn't support this")
    }

    @Throws(UnsupportedOperationException::class)
    fun addWaitSemaphore(vkSemaphore: Long, dstStageMask: Int) {
        throw UnsupportedOperationException("This implementation (${this::class.java.simpleName}) doesn't support this")
    }

    fun drawString(
        minX: Float, yBottom: Float, maxX: Float, yTop: Float,
        string: String, style: TextStyle, dryRun: Boolean = false,
        suggestLeftToRight: Boolean = true
    ): List<CharacterPosition>

    fun getStringAspectRatio(string: String, fontReference: FontReference?): Float

    /**
     * Returns the width of this target divided by the height of this target
     */
    fun getAspectRatio(): Float
}
