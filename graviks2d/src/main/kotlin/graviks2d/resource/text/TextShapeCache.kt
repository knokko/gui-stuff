package graviks2d.resource.text

import com.github.knokko.boiler.buffer.MappedVmaBuffer
import com.github.knokko.boiler.buffer.VmaBuffer
import com.github.knokko.boiler.images.VmaImage
import graviks2d.context.GraviksContext
import graviks2d.pipeline.text.*
import graviks2d.pipeline.text.OPERATION_CORRECT_CONTROL
import graviks2d.pipeline.text.OPERATION_CORRECT_END
import graviks2d.pipeline.text.OPERATION_CORRECT_START
import graviks2d.pipeline.text.OPERATION_INCREMENT
import graviks2d.pipeline.text.TextCountVertex
import graviks2d.pipeline.text.TextVertexBuffer
import org.lwjgl.stb.STBRPContext
import org.lwjgl.stb.STBRPNode
import org.lwjgl.stb.STBRPRect
import org.lwjgl.stb.STBRectPack.stbrp_init_target
import org.lwjgl.stb.STBRectPack.stbrp_pack_rects
import org.lwjgl.stb.STBTruetype.STBTT_vcurve
import org.lwjgl.stb.STBTruetype.STBTT_vline
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memByteBuffer
import org.lwjgl.system.MemoryUtil.memFloatBuffer
import org.lwjgl.util.vma.Vma.*
import org.lwjgl.vulkan.VK10.*

internal class TextShapeCache(
    val context: GraviksContext,
    val width: Int,
    val height: Int,
    private val vertexBufferSize: Int,
    rectanglePackingBufferSize: Int,
    rectanglePackingNodeBufferSize: Int
) {

    val textCountAtlas: VmaImage
    val textOddAtlas: VmaImage

    val textAtlasFramebuffer: Long

    val countVertexBuffer: MappedVmaBuffer
    val oddVertexBuffer: VmaBuffer

    val descriptorPool: Long
    val descriptorSet: Long

    private val rectanglePackingContext: STBRPContext
    private val rectanglePackingBuffer: STBRPRect.Buffer
    private val rectanglePackingNodes: STBRPNode.Buffer
    private var currentRectanglePackingIndex = 0

    private val cachedCharacters = mutableMapOf<TextCacheKey, TextCacheArea>()
    var currentVertexIndex = 0

    init {
        /*
         * The size of the text cache must be at least as large as the size of the
         * context. If it were smaller, it would not be able to draw very big
         * characters that fill the entire context image.
         */
        if (this.width < this.context.width) {
            throw IllegalArgumentException("Width ($width) can't be smaller than context width ${this.context.width}")
        }
        if (this.height < this.context.height) {
            throw IllegalArgumentException("Height ($height) can't be smaller than context height ${this.context.height}")
        }

        this.rectanglePackingNodes = STBRPNode.calloc(rectanglePackingNodeBufferSize)

        this.rectanglePackingContext = STBRPContext.calloc()
        stbrp_init_target(this.rectanglePackingContext, width, height, this.rectanglePackingNodes)
        this.rectanglePackingBuffer = STBRPRect.calloc(rectanglePackingBufferSize)

        val boiler = context.instance.boiler
        stackPush().use { stack ->
            this.textCountAtlas = boiler.images.createSimple(
                stack, this.width, this.height, TEXT_COLOR_FORMAT,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT or VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT,
                VK_IMAGE_ASPECT_COLOR_BIT, "GraviksTextCountAtlas"
            )
            this.textOddAtlas = boiler.images.createSimple(
                stack, this.width, this.height, TEXT_COLOR_FORMAT,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT or VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_IMAGE_ASPECT_COLOR_BIT, "GraviksTextOddAtlas"
            )

            val vertexBufferByteSize = this.vertexBufferSize.toLong() * TextCountVertex.BYTE_SIZE
            if (vertexBufferByteSize > Int.MAX_VALUE) {
                throw IllegalArgumentException("vertexBufferSize ($vertexBufferSize vertices -> $vertexBufferByteSize bytes) is too large")
            }
            this.countVertexBuffer = boiler.buffers.createMapped(
                vertexBufferByteSize, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, "GraviksTextCountVertices"
            )

            val oddVertexBuffer = boiler.buffers.createMapped(
                6L * 2 * 4, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, "GraviksTextOddVertices"
            )
            this.oddVertexBuffer = oddVertexBuffer.asBuffer()

            val hostVertexOddByteBuffer = memFloatBuffer(oddVertexBuffer.hostAddress, 6 * 2)
            hostVertexOddByteBuffer.put(0, -1f)
            hostVertexOddByteBuffer.put(1, -1f)
            hostVertexOddByteBuffer.put(2, 1f)
            hostVertexOddByteBuffer.put(3, -1f)
            hostVertexOddByteBuffer.put(4, 1f)
            hostVertexOddByteBuffer.put(5, 1f)
            hostVertexOddByteBuffer.put(6, 1f)
            hostVertexOddByteBuffer.put(7, 1f)
            hostVertexOddByteBuffer.put(8, -1f)
            hostVertexOddByteBuffer.put(9, 1f)
            hostVertexOddByteBuffer.put(10, -1f)
            hostVertexOddByteBuffer.put(11, -1f)

            this.textAtlasFramebuffer = boiler.images.createFramebuffer(
                stack, context.instance.textPipelines.vkRenderPass, width, height, "GraviksTextFramebuffer",
                textCountAtlas.vkImageView, textOddAtlas.vkImageView
            )

            val (descriptorPool, descriptorSet) = createTextOddPipelineDescriptors(
                boiler, stack, this.context.instance.textPipelines.oddDescriptorSetLayout,
                this.textCountAtlas.vkImageView
            )
            this.descriptorPool = descriptorPool
            this.descriptorSet = descriptorSet
        }
    }

    private fun claimCacheArea(width: Int, height: Int): TextCacheArea? {
        if (this.currentRectanglePackingIndex == this.rectanglePackingBuffer.capacity()) {
            return null
        }

        val claimedRectangleIndex = this.currentRectanglePackingIndex
        this.currentRectanglePackingIndex += 1
        this.rectanglePackingBuffer.position(claimedRectangleIndex)
        this.rectanglePackingBuffer.limit(this.currentRectanglePackingIndex)

        this.rectanglePackingBuffer[claimedRectangleIndex].w(width)
        this.rectanglePackingBuffer[claimedRectangleIndex].h(height)

        val succeeded = stbrp_pack_rects(this.rectanglePackingContext, this.rectanglePackingBuffer)

        if (succeeded == 0) return null
        if (!this.rectanglePackingBuffer[claimedRectangleIndex].was_packed()) {
            throw RuntimeException("Rectangle should have been packed")
        }

        val x = this.rectanglePackingBuffer[claimedRectangleIndex].x()
        val y = this.rectanglePackingBuffer[claimedRectangleIndex].y()

        return TextCacheArea(
            x.toFloat() / this.width.toFloat(),
            y.toFloat() / this.height.toFloat(),
            (x + width).toFloat() / this.width.toFloat(),
            (y + height).toFloat() / this.height.toFloat()
        )
    }

    /**
     * Returns null if the given shape is not cached and doesn't fit in this cache.
     * In this case, the context will need to perform a hard flush, clear the cache,
     * and try again.
     */
    fun prepareCharacter(
        codepoint: Int, fontAscent: Int, fontDescent: Int,
        glyphShape: GlyphShape, width: Int, height: Int
    ): TextCacheArea? {
        if (glyphShape.ttfVertices == null) throw IllegalArgumentException("Don't use this method on empty glyphs")

        val cacheKey = TextCacheKey(
            codepoint, width, height
        )
        val cachedResult = this.cachedCharacters[cacheKey]
        if (cachedResult != null) return cachedResult

        var numVertices = 0
        for (ttfVertex in glyphShape.ttfVertices) {
            if (ttfVertex.type() == STBTT_vline) numVertices += 3
            if (ttfVertex.type() == STBTT_vcurve) numVertices += 6
        }

        if (this.currentVertexIndex + numVertices > this.vertexBufferSize) return null

        val cacheArea: TextCacheArea = this.claimCacheArea(width, height) ?: return null

        this.cachedCharacters[cacheKey] = cacheArea

        var prevX = 0f
        var prevY = 0f

        for (ttfVertex in glyphShape.ttfVertices) {
            ttfVertex.run {
                fun transformPoint(x: Short, y: Short): Pair<Float, Float> {
                    val localX = (x.toFloat() + 0.5f) / glyphShape.advanceWidth.toFloat()
                    val localY = (y.toFloat() + 0.5f - fontDescent.toFloat()) / (fontAscent - fontDescent).toFloat()

                    return Pair(
                        cacheArea.minX + localX * cacheArea.width,
                        cacheArea.minY + localY * cacheArea.height
                    )
                }

                val (currentX, currentY) = transformPoint(x(), y())
                val hostVertexBuffer = TextVertexBuffer.createAtBuffer(
                    memByteBuffer(countVertexBuffer.hostAddress, countVertexBuffer.size.toInt()),
                    vertexBufferSize
                )

                if (type() == STBTT_vline || type() == STBTT_vcurve) {
                    hostVertexBuffer[currentVertexIndex].run {
                        this.x = prevX
                        this.y = prevY
                        this.operation = OPERATION_INCREMENT
                    }
                    hostVertexBuffer[currentVertexIndex + 1].run {
                        this.x = currentX
                        this.y = currentY
                        this.operation = OPERATION_INCREMENT
                    }
                    hostVertexBuffer[currentVertexIndex + 2].run {
                        this.x = 0f
                        this.y = 0f
                        this.operation = OPERATION_INCREMENT
                    }
                    currentVertexIndex += 3
                }
                if (type() == STBTT_vcurve) {
                    val (controlX, controlY) = transformPoint(cx(), cy())
                    hostVertexBuffer[currentVertexIndex].run {
                        this.x = prevX
                        this.y = prevY
                        this.operation = OPERATION_CORRECT_START
                    }
                    hostVertexBuffer[currentVertexIndex + 1].run {
                        this.x = controlX
                        this.y = controlY
                        this.operation = OPERATION_CORRECT_CONTROL
                    }
                    hostVertexBuffer[currentVertexIndex + 2].run {
                        this.x = currentX
                        this.y = currentY
                        this.operation = OPERATION_CORRECT_END
                    }
                    currentVertexIndex += 3
                }
                prevX = currentX
                prevY = currentY
            }
        }

        return cacheArea
    }

    fun clear() {
        this.currentVertexIndex = 0
        this.cachedCharacters.clear()
        this.currentRectanglePackingIndex = 0
        stbrp_init_target(this.rectanglePackingContext, this.width, this.height, this.rectanglePackingNodes)
    }

    fun destroy() {
        val vmaAllocator = this.context.instance.boiler.vmaAllocator()
        val vkDevice = this.context.instance.boiler.vkDevice()

        vkDestroyFramebuffer(vkDevice, this.textAtlasFramebuffer, null)
        vmaDestroyBuffer(vmaAllocator, this.countVertexBuffer.vkBuffer, this.countVertexBuffer.vmaAllocation)
        vmaDestroyBuffer(vmaAllocator, this.oddVertexBuffer.vkBuffer, this.oddVertexBuffer.vmaAllocation)
        vkDestroyImageView(vkDevice, this.textOddAtlas.vkImageView, null)
        vmaDestroyImage(vmaAllocator, this.textOddAtlas.vkImage, this.textOddAtlas.vmaAllocation)
        vkDestroyImageView(vkDevice, this.textCountAtlas.vkImageView, null)
        vmaDestroyImage(vmaAllocator, this.textCountAtlas.vkImage, this.textCountAtlas.vmaAllocation)
        vkDestroyDescriptorPool(vkDevice, this.descriptorPool, null)

        this.rectanglePackingBuffer.free()
        this.rectanglePackingNodes.free()
        this.rectanglePackingContext.free()
    }
}
