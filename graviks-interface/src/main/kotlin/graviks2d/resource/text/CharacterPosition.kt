package graviks2d.resource.text

class CharacterPosition(
    /**
     * The minimum X-coordinate of the left-most character. Unlike `croppedMinX`, this may be outside the
     * string bounds.
     */
    val minX: Float,
    val minY: Float,
    /**
     * The maximum X-coordinate of the right-most character. Unlike `croppedMaxX`, this may be outside the
     * string bounds.
     */
    val maxX: Float,
    val maxY: Float,
    val isLeftToRight: Boolean,
    /**
     * The minimum X-coordinate of the left-most character. Unlike `minX`, this will never be outside the
     * string bounds.
     */
    val croppedMinX: Float = minX,
    /**
     * The maximum X-coordinate of the right-most character. Unlike `maxX`, this will never be outside the
     * string bounds.
     */
    val croppedMaxX: Float = maxX,
)
