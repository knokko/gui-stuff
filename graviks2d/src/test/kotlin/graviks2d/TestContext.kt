package graviks2d

import com.github.knokko.boiler.builder.BoilerBuilder
import com.github.knokko.boiler.builder.instance.ValidationFeatures
import com.github.knokko.boiler.commands.CommandRecorder
import com.github.knokko.boiler.exceptions.VulkanFailureException.assertVkSuccess
import com.github.knokko.boiler.images.VmaImage
import com.github.knokko.boiler.sync.ResourceUsage
import graviks2d.context.GraviksContext
import graviks2d.core.GraviksInstance
import graviks2d.resource.image.ImageCache
import graviks2d.resource.image.ImageReference
import graviks2d.resource.image.createImagePair
import graviks2d.resource.text.TextStyle
import graviks2d.target.GraviksScissor
import graviks2d.util.Color
import graviks2d.util.HostImage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.lwjgl.stb.STBImage.stbi_info_from_memory
import org.lwjgl.stb.STBImage.stbi_load_from_memory
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.util.vma.Vma.*
import org.lwjgl.vulkan.VK10.*
import org.opentest4j.AssertionFailedError
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.math.absoluteValue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestContext {

    private val boiler = BoilerBuilder(
        VK_API_VERSION_1_0, "TestGraviksContext", VK_MAKE_VERSION(0, 5, 0)
    )
        .validation(ValidationFeatures(false, false, false, true, true))
        .build()
    private val graviksInstance = GraviksInstance(boiler)

    private fun withTestImage(context: GraviksContext, flipY: Boolean, test: (() -> Unit, HostImage) -> Unit) {
        val testBuffer = boiler.buffers.createMapped(
            context.width * context.height * 4L, VK_BUFFER_USAGE_TRANSFER_DST_BIT, "TestReadback"
        )

        val testHostBuffer = memByteBuffer(testBuffer.hostAddress, context.width * context.height * 4)

        test({ context.copyColorImageTo(
            destImage = null, destBuffer = testBuffer.vkBuffer, destImageFormat = null, shouldAwaitCompletion = true
        )}, HostImage(context.width, context.height, testHostBuffer, flipY))

        vmaDestroyBuffer(boiler.vmaAllocator(), testBuffer.vkBuffer, testBuffer.vmaAllocation)
    }

    private fun assertImageEquals(expectedFileName: String, actual: HostImage) {
        val expectedInput = this::class.java.classLoader.getResourceAsStream("graviks2d/images/expected/$expectedFileName")!!
        val expectedByteArray = expectedInput.readAllBytes()
        expectedInput.close()

        val expectedByteBuffer = memCalloc(expectedByteArray.size)
        expectedByteBuffer.put(0, expectedByteArray)

        stackPush().use { stack ->
            val pWidth = stack.callocInt(1)
            val pHeight = stack.callocInt(1)
            val pChannels = stack.callocInt(1)

            assertTrue(stbi_info_from_memory(expectedByteBuffer, pWidth, pHeight, pChannels))
            val expectedHostBuffer = stbi_load_from_memory(expectedByteBuffer, pWidth, pHeight, pChannels, 4)!!

            val expectedHostImage = HostImage(pWidth[0], pHeight[0], expectedHostBuffer, true)
            assertImageEquals(expectedHostImage, actual)
            memFree(expectedHostBuffer)
        }
        memFree(expectedByteBuffer)
    }

    private fun assertImageEquals(expected: HostImage, actual: HostImage) {
        assertEquals(expected.width, actual.width)
        assertEquals(expected.height, actual.height)

        try {
            for (x in 0 until expected.width) {
                for (y in 0 until expected.height) {
                    assertColorEquals(expected.getPixel(x, y), actual.getPixel(x, y))
                }
            }
        } catch (failed: AssertionFailedError) {

            // This should make debugging a lot easier
            val time = System.nanoTime()
            expected.saveToDisk(File("expected-$time.png"))
            actual.saveToDisk(File("actual-$time.png"))

            throw failed
        }
    }

    private fun assertColorEquals(expected: Color, actual: Color) {
        fun assertComponentEquals(expectedValue: Int, actualValue: Int) {
            assertTrue(
                (expectedValue - actualValue).absoluteValue <= 2,
                "expected (${expected.red},${expected.green},${expected.blue},${expected.alpha}) " +
                        "but got (${actual.red},${actual.green},${actual.blue},${actual.alpha})"
            )
        }
        assertComponentEquals(expected.red, actual.red)
        assertComponentEquals(expected.green, actual.green)
        assertComponentEquals(expected.blue, actual.blue)
        assertComponentEquals(expected.alpha, actual.alpha)
    }

    @Test
    fun testFillTriangle() {
        val backgroundColor = Color.rgbInt(100, 0, 0)
        val graviks = GraviksContext(
            this.graviksInstance, 100, 100, initialBackgroundColor = backgroundColor
        )

        val color = Color.GREEN

        withTestImage(graviks, true) { update, hostImage ->
            graviks.fillTriangle(0f, 0.1f, 0.5f, 0.2f, 0.2f, 0.8f, color)
            update()
            assertColorEquals(backgroundColor, hostImage.getPixel(0, 0))
            assertColorEquals(color, hostImage.getPixel(2, 12))
            assertColorEquals(color, hostImage.getPixel(48, 20))
            assertColorEquals(backgroundColor, hostImage.getPixel(52, 20))
            assertColorEquals(color, hostImage.getPixel(20, 75))
            assertColorEquals(backgroundColor, hostImage.getPixel(20, 85))
        }

        graviks.destroy()
    }

    @Test
    fun testSetScissor() {
        val backgroundColor = Color.rgbInt(100, 0, 0)
        val graviks = GraviksContext(
            this.graviksInstance, 10, 10, initialBackgroundColor = backgroundColor
        )

        withTestImage(graviks, true) { update, hostImage ->
            graviks.setScissor(GraviksScissor(0.1f, 0.2f, 0.3f, 0.4f))
            graviks.fillRect(0f, 0f, 0.2f, 1f, Color.RED)
            graviks.fillRect(0.2f, 0f, 1f, 1f, Color.GREEN)
            graviks.setScissor(GraviksScissor(0.5f, 0.5f, 1f, 1f))
            graviks.fillRect(0.2f, 0.4f, 0.8f, 0.9f, Color.BLUE)
            update()
            for (x in 0 until 10) {
                assertColorEquals(backgroundColor, hostImage.getPixel(x, 0))
                assertColorEquals(backgroundColor, hostImage.getPixel(x, 1))
                assertColorEquals(backgroundColor, hostImage.getPixel(x, 4))
                assertColorEquals(backgroundColor, hostImage.getPixel(x, 9))
            }
            for (y in 2 until 4) {
                assertColorEquals(backgroundColor, hostImage.getPixel(0, y))
                assertColorEquals(Color.RED, hostImage.getPixel(1, y))
                assertColorEquals(Color.GREEN, hostImage.getPixel(2, y))
                for (x in 3 until 10) {
                    assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
                }
            }
            for (y in 5 until 9) {
                assertColorEquals(backgroundColor, hostImage.getPixel(0, y))
                assertColorEquals(backgroundColor, hostImage.getPixel(1, y))
                for (x in 5 until 8) {
                    assertColorEquals(Color.BLUE, hostImage.getPixel(x, y))
                }
                assertColorEquals(backgroundColor, hostImage.getPixel(8, y))
                assertColorEquals(backgroundColor, hostImage.getPixel(9, y))
            }
        }

        graviks.destroy()
    }

    @Test
    fun testFillRectangle() {
        val backgroundColor = Color.rgbInt(100, 0, 0)
        val graviks = GraviksContext(
            this.graviksInstance, 20, 20, initialBackgroundColor = backgroundColor
        )

        withTestImage(graviks, false) { update, hostImage ->
            graviks.fillRect(0.15f, 0.35f, 0.35f, 0.8f, Color.rgbInt(50, 100, 0))
            graviks.fillRect(0.7f, 0.4f, 0.25f, 0.2f, Color.rgbInt(0, 100, 50))
            graviks.fillRect(0.8f, 0.3f, 0.85f, 0.9f, Color.rgbInt(0, 150, 250))
            graviks.fillRect(0f, 0.85f, 0.5f, 0.7f, Color.rgbInt(200, 200, 0))
            graviks.fillRect(0.95f, 0.85f, 0.3f, 0.9f, Color.BLACK)
            graviks.fillRect(0.4f, 0.55f, 0.45f, 0.6f, Color.rgbInt(200, 0, 200))
            update()
            assertImageEquals("fillRectangle.png", hostImage)
        }

        graviks.destroy()
    }

    @Test
    fun testDrawRect() {
        val backgroundColor = Color.BLUE
        val lineColor = Color.RED
        val graviks = GraviksContext(
                this.graviksInstance, 200, 200, initialBackgroundColor = backgroundColor
        )

        withTestImage(graviks, true) { update, hostImage ->
            graviks.drawRect(0.1f, 0.2f, 0.7f, 0.9f, 0.05f, lineColor)
            update()

            // Test that the area outside the domain is still filled with the background color
            for (x in 0 until 13) {
                for (y in 0 until graviks.height) assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
            }
            for (x in 157 until 200) {
                for (y in 0 until graviks.height) assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
            }
            for (y in 0 until 33) {
                for (x in 0 until graviks.width) assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
            }
            for (y in 187 until 200) {
                for (x in 0 until graviks.width) assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
            }

            // Test that the area inside the domain is still filled with the background color
            for (x in 27 until 133) {
                for (y in 47 until 173) assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
            }

            // Test that the lines between the corners are exactly equal to the line color
            for (x in 20 .. 140) {
                assertColorEquals(lineColor, hostImage.getPixel(x, 40))
                assertColorEquals(lineColor, hostImage.getPixel(x, 180))
            }
            for (y in 40 .. 180) {
                assertColorEquals(lineColor, hostImage.getPixel(20, y))
                assertColorEquals(lineColor, hostImage.getPixel(140, y))
            }
        }

        graviks.destroy()
    }

    @Test
    fun testFillRoundedRect() {
        val backgroundColor = Color.rgbInt(100, 0, 0)
        val graviks = GraviksContext(
            this.graviksInstance, 100, 100, initialBackgroundColor = backgroundColor
        )

        withTestImage(graviks, true) { update, hostImage ->
            val rectColor = Color.rgbInt(0, 100, 0)
            graviks.fillRoundedRect(0.1f, 0.2f, 0.6f, 0.4f, 0.1f, rectColor)
            update()

            // Left side
            assertColorEquals(backgroundColor, hostImage.getPixel(11, 21))
            assertColorEquals(backgroundColor, hostImage.getPixel(11, 38))
            for (x in 11 until 23) assertColorEquals(rectColor, hostImage.getPixel(x, 30))

            // Middle
            assertColorEquals(backgroundColor, hostImage.getPixel(35, 18))
            for (y in 21 .. 38) assertColorEquals(rectColor, hostImage.getPixel(35, y))
            assertColorEquals(backgroundColor, hostImage.getPixel(35, 41))

            // Right
            assertColorEquals(backgroundColor, hostImage.getPixel(58, 21))
            assertColorEquals(backgroundColor, hostImage.getPixel(58, 38))
            for (x in 48 until 58) assertColorEquals(rectColor, hostImage.getPixel(x, 30))
        }

        graviks.destroy()
    }

    @Test
    fun testDrawRoundedRect() {
        val backgroundColor = Color.rgbInt(100, 0, 0)
        val graviks = GraviksContext(this.graviksInstance, 100, 100, initialBackgroundColor = backgroundColor)

        withTestImage(graviks, true) { update, hostImage ->
            val rectColor = Color.rgbInt(0, 100, 0)
            graviks.drawRoundedRect(0.1f, 0.2f, 0.6f, 0.4f, 0.1f, 0.95f, rectColor)
            update()

            // Left side
            assertColorEquals(backgroundColor, hostImage.getPixel(11, 21))
            assertColorEquals(backgroundColor, hostImage.getPixel(11, 38))
            assertColorEquals(rectColor, hostImage.getPixel(14, 30))

            // Middle
            assertColorEquals(backgroundColor, hostImage.getPixel(35, 18))
            assertColorEquals(rectColor, hostImage.getPixel(35, 24))
            assertColorEquals(backgroundColor, hostImage.getPixel(35, 30))
            assertColorEquals(rectColor, hostImage.getPixel(35, 35))
            assertColorEquals(backgroundColor, hostImage.getPixel(35, 41))

            // Right
            assertColorEquals(backgroundColor, hostImage.getPixel(58, 21))
            assertColorEquals(backgroundColor, hostImage.getPixel(58, 38))
            assertColorEquals(rectColor, hostImage.getPixel(55, 30))
        }

        graviks.destroy()
    }

    @Test
    fun testDrawImage() {
        val backgroundColor = Color.WHITE
        val graviks = GraviksContext(this.graviksInstance, 50, 50, initialBackgroundColor = backgroundColor)

        val image1 = ImageReference.classLoaderPath("graviks2d/images/test1.png", false)
        val image2 = ImageReference.classLoaderPath("graviks2d/images/test2.png", false)

        withTestImage(graviks, true) { update, hostImage ->
            fun drawFlippedImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, image: ImageReference) {
                graviks.drawImage(xLeft, 1f - yTop, xRight, 1f - yBottom, image)
            }

            drawFlippedImage(0.04f, 0.06f, 0.46f, 0.48f, image2)
            drawFlippedImage(0.14f, 0.34f, 0.56f, 0.76f, image2)
            drawFlippedImage(0.6f, 0.58f, 0.88f, 0.86f, image1)
            drawFlippedImage(0.32f, 0.22f, 0.6f, 0.5f, image1)
            drawFlippedImage(0.54f, 0.22f, 0.96f, 0.64f, image2)
            update()
            assertImageEquals("drawImage.png", hostImage)
        }

        graviks.destroy()
    }

    @Test
    fun testDrawVulkanImage() {
        val backgroundColor = Color.WHITE
        val graviks = GraviksContext(this.graviksInstance, 50, 50, initialBackgroundColor = backgroundColor)

        val inputImage1 = TestContext::class.java.classLoader.getResourceAsStream("graviks2d/images/test1.png")!!
        val image1 = createImagePair(this.graviksInstance, inputImage1, "test1")
        inputImage1.close()

        val inputImage2 = TestContext::class.java.classLoader.getResourceAsStream("graviks2d/images/test2.png")!!
        val image2 = createImagePair(this.graviksInstance, inputImage2, "test1")
        inputImage2.close()

        withTestImage(graviks, true) { update, hostImage ->
            fun drawFlippedImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, image: VmaImage) {
                graviks.drawVulkanImage(xLeft, 1f - yTop, xRight, 1f - yBottom, image.vkImage, image.vkImageView)
            }

            drawFlippedImage(0.04f, 0.06f, 0.46f, 0.48f, image2)
            drawFlippedImage(0.14f, 0.34f, 0.56f, 0.76f, image2)
            drawFlippedImage(0.6f, 0.58f, 0.88f, 0.86f, image1)
            drawFlippedImage(0.32f, 0.22f, 0.6f, 0.5f, image1)
            drawFlippedImage(0.54f, 0.22f, 0.96f, 0.64f, image2)
            update()
            assertImageEquals("drawImage.png", hostImage)
        }

        vkDestroyImageView(boiler.vkDevice(), image1.vkImageView, null)
        vkDestroyImageView(boiler.vkDevice(), image2.vkImageView, null)
        vmaDestroyImage(boiler.vmaAllocator(), image1.vkImage, image1.vmaAllocation)
        vmaDestroyImage(boiler.vmaAllocator(), image2.vkImage, image2.vmaAllocation)

        graviks.destroy()
    }

    @Test
    fun testDrawVulkanImageCombinedWithBasicImage() {
        val backgroundColor = Color.WHITE
        val graviks = GraviksContext(this.graviksInstance, 50, 50, initialBackgroundColor = backgroundColor)

        val inputImage1 = TestContext::class.java.classLoader.getResourceAsStream("graviks2d/images/test1.png")!!
        val image1 = createImagePair(this.graviksInstance, inputImage1, "test1")
        inputImage1.close()

        val image2 = ImageReference.classLoaderPath("graviks2d/images/test2.png", false)

        withTestImage(graviks, true) { update, hostImage ->
            fun drawFlippedImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, image: ImageReference) {
                graviks.drawImage(xLeft, 1f - yTop, xRight, 1f - yBottom, image)
            }

            fun drawFlippedImage(xLeft: Float, yBottom: Float, xRight: Float, yTop: Float, image: VmaImage) {
                graviks.drawVulkanImage(xLeft, 1f - yTop, xRight, 1f - yBottom, image.vkImage, image.vkImageView)
            }

            drawFlippedImage(0.04f, 0.06f, 0.46f, 0.48f, image2)
            drawFlippedImage(0.14f, 0.34f, 0.56f, 0.76f, image2)
            drawFlippedImage(0.6f, 0.58f, 0.88f, 0.86f, image1)
            drawFlippedImage(0.32f, 0.22f, 0.6f, 0.5f, image1)
            drawFlippedImage(0.54f, 0.22f, 0.96f, 0.64f, image2)
            update()
            assertImageEquals("drawImage.png", hostImage)
        }

        vkDestroyImageView(boiler.vkDevice(), image1.vkImageView, null)
        vmaDestroyImage(boiler.vmaAllocator(), image1.vkImage, image1.vmaAllocation)

        graviks.destroy()
    }

    @Test
    fun testDrawString() {
        val backgroundColor = Color.WHITE
        val graviks = GraviksContext(this.graviksInstance, 300, 600, initialBackgroundColor = backgroundColor)

        val simpleStyle = TextStyle(
            fillColor = Color.rgbInt(0, 0, 150),
            font = null
        )

        val complexStyle = TextStyle(
                fillColor = Color.RED,
                fillColorFunction = { index -> Color.rgbInt(0, 50 + 50 * index, 0) },
                font = null
        )

        withTestImage(graviks, true) { update, hostImage ->
            // Test that drawing an empty string won't crash and has no effect
            assertTrue(graviks.drawString(0.1f, 0.2f, 0.3f, 0.4f, "", simpleStyle).isEmpty())
            update()
            for (x in 0 until hostImage.width) {
                for (y in 0 until hostImage.height) {
                    assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
                }
            }

            val characterPositions = graviks.drawString(0.1f, 0.1f, 0.9f, 0.4f, "T E", simpleStyle)
            assertEquals(3, characterPositions.size)
            for (position in characterPositions) {
                assertTrue(position.maxX - position.minX > 0.1f)
                assertTrue(position.maxX - position.minX < 0.5f)
                assertTrue(position.maxY - position.minY > 0.1f)
                assertTrue(position.maxY - position.minY < 0.7f)
                assertTrue(position.minX > 0.09f)
                assertTrue(position.minY > 0.09f)
                assertTrue(position.maxX < 0.91f)
                assertTrue(position.maxY < 0.91f)
            }
            for (index in 0 until 2) {
                assertTrue(characterPositions[index].maxX < characterPositions[index + 1].minX + 0.01f)
            }
            update()

            fun countSplittedOccurrences(x: Int, textColor: Color): Int {
                var counter = 0
                var wasTextColor = false
                for (y in 0 until graviks.height) {
                    val isTextColor = hostImage.getPixel(x, y) == textColor
                    if (isTextColor && !wasTextColor) counter += 1
                    wasTextColor = isTextColor
                }
                return counter
            }

            fun countAllOccurrences(x: Int, textColor: Color): Int {
                return (0 until graviks.height).count { y -> hostImage.getPixel(x, y) == textColor }
            }

            var tBarLeftIndex = (0 until graviks.width).indexOfFirst { x -> countAllOccurrences(x, simpleStyle.fillColor) > 50 }
            var tBarRightIndex = (0 until graviks.width).indexOfFirst { x -> x > tBarLeftIndex && countAllOccurrences(x, simpleStyle.fillColor) < 50 } - 1
            assertEquals(2, countSplittedOccurrences(tBarLeftIndex - 2, simpleStyle.fillColor))
            assertEquals(2, countSplittedOccurrences(tBarRightIndex + 2, simpleStyle.fillColor))
            assertEquals(1, countSplittedOccurrences(tBarLeftIndex - 25, simpleStyle.fillColor))
            assertEquals(1, countSplittedOccurrences(tBarRightIndex + 25, simpleStyle.fillColor))
            assertTrue(tBarLeftIndex < tBarRightIndex)
            assertTrue(tBarLeftIndex > characterPositions[0].minX * graviks.width)
            assertTrue(tBarRightIndex < characterPositions[0].maxX * graviks.height)

            var eBarRightIndex = (0 until graviks.width).indexOfLast { x -> countAllOccurrences(x, simpleStyle.fillColor) > 50 }
            var eBarLeftIndex = (0 until eBarRightIndex).indexOfLast { x -> countAllOccurrences(x, simpleStyle.fillColor) < 50 } + 1
            assertEquals(0, countSplittedOccurrences(eBarLeftIndex - 20, simpleStyle.fillColor))
            assertEquals(2, countSplittedOccurrences(eBarLeftIndex - 2, simpleStyle.fillColor))
            assertEquals(3, countSplittedOccurrences(eBarRightIndex + 2, simpleStyle.fillColor))
            assertEquals(3, countSplittedOccurrences(eBarRightIndex + 20, simpleStyle.fillColor))
            assertTrue(eBarLeftIndex < eBarRightIndex)
            assertTrue(eBarRightIndex > characterPositions[2].minX * graviks.width)
            assertTrue(eBarRightIndex < characterPositions[2].maxX * graviks.width)

            for (x in 0 until graviks.width) {
                for (y in graviks.height / 2 until graviks.height) {
                    assertColorEquals(backgroundColor, hostImage.getPixel(x, y))
                }
            }

            // Test drawing the complex style
            graviks.fillRect(0f, 0f, 1f, 1f, backgroundColor)
            graviks.drawString(0.1f, 0.1f, 0.9f, 0.4f, "T E", complexStyle)
            update()

            val fillColorT = Color.rgbInt(0, 50, 0)
            val fillColorE = Color.rgbInt(0, 150, 0)

            tBarLeftIndex = (0 until graviks.width).indexOfFirst { x -> countAllOccurrences(x, fillColorT) > 50 }
            tBarRightIndex = (0 until graviks.width).indexOfFirst { x -> x > tBarLeftIndex && countAllOccurrences(x, fillColorT) < 50 } - 1
            assertEquals(2, countSplittedOccurrences(tBarLeftIndex - 2, fillColorT))
            assertEquals(2, countSplittedOccurrences(tBarRightIndex + 2, fillColorT))
            assertEquals(1, countSplittedOccurrences(tBarLeftIndex - 25, fillColorT))
            assertEquals(1, countSplittedOccurrences(tBarRightIndex + 25, fillColorT))
            assertTrue(tBarLeftIndex < tBarRightIndex)
            assertTrue(tBarLeftIndex > characterPositions[0].minX * graviks.width)
            assertTrue(tBarRightIndex < characterPositions[0].maxX * graviks.height)

            eBarRightIndex = (0 until graviks.width).indexOfLast { x -> countAllOccurrences(x, fillColorE) > 50 }
            eBarLeftIndex = (0 until eBarRightIndex).indexOfLast { x -> countAllOccurrences(x, fillColorE) < 50 } + 1
            assertEquals(0, countSplittedOccurrences(eBarLeftIndex - 20, fillColorE))
            assertEquals(2, countSplittedOccurrences(eBarLeftIndex - 2, fillColorE))
            assertEquals(3, countSplittedOccurrences(eBarRightIndex + 2, fillColorE))
            assertEquals(3, countSplittedOccurrences(eBarRightIndex + 20, fillColorE))
            assertTrue(eBarLeftIndex < eBarRightIndex)
            assertTrue(eBarRightIndex > characterPositions[2].minX * graviks.width)
            assertTrue(eBarRightIndex < characterPositions[2].maxX * graviks.width)
        }

        graviks.destroy()
    }

    @Test
    fun testImageCache() {
        val testImageFile = Files.createTempFile(null, null).toFile()
        ImageIO.write(BufferedImage(4, 6, TYPE_INT_ARGB), "PNG", testImageFile)

        runBlocking {
            val cache = ImageCache(graviksInstance, softImageLimit = 2)

            val image10 = ImageReference.file(testImageFile)
            val image11 = ImageReference.file(testImageFile)

            val image20 = ImageReference.classLoaderPath("graviks2d/images/test1.png", false)
            val image21 = ImageReference.classLoaderPath("graviks2d/images/test1.png", false)

            val image30 = ImageReference.classLoaderPath("graviks2d/images/test2.png", false)

            // Initially, the cache should be empty
            assertEquals(0, cache.getCurrentCacheSize())

            // After borrowing 1 image, the cache should have size 1
            val borrowed10 = cache.borrowImage(image10)
            assertEquals(1, cache.getCurrentCacheSize())

            // Borrowing the same image again should not increase the cache size
            val borrowed11 = cache.borrowImage(image11)
            assertEquals(1, cache.getCurrentCacheSize())

            // Awaiting should not increase the cache size either
            borrowed10.imagePair.await()
            borrowed11.imagePair.await()
            assertEquals(1, cache.getCurrentCacheSize())

            // Borrowing another image should increase the cache size
            val borrowed20 = cache.borrowImage(image20)
            val borrowed21 = cache.borrowImage(image21)
            assertEquals(2, cache.getCurrentCacheSize())

            // Returning the other images should not decrease the cache size
            // when the image limit is not reached
            cache.returnImage(borrowed20)

            // Awaiting the image before returning is optional
            borrowed21.imagePair.await()
            cache.returnImage(borrowed21)
            assertEquals(2, cache.getCurrentCacheSize())

            // The second image should be removed from the cache when adding
            // a third image to the cache
            val borrowed30 = cache.borrowImage(image30)
            assertEquals(2, cache.getCurrentCacheSize())

            // When borrowing the second image again, the cache limit must be
            // exceeded because all images are still in use
            val borrowed22 = cache.borrowImage(image21)
            assertEquals(3, cache.getCurrentCacheSize())

            // But the third image should be removed as soon as it is returned
            // since the cache limit is already exceeded
            cache.returnImage(borrowed30)
            assertEquals(2, cache.getCurrentCacheSize())

            // Let's return all borrowed images
            cache.returnImage(borrowed10)
            cache.returnImage(borrowed11)
            cache.returnImage(borrowed22)

            // Since the cache limit is no longer exceeded, the size should remain 2
            assertEquals(2, cache.getCurrentCacheSize())

            // But destroying the cache should set the size to 0
            cache.destroy()
            assertEquals(0, cache.getCurrentCacheSize())
        }
    }

    @Test
    fun testDrawImageDescriptorManagement() {
        val backgroundColor = Color.WHITE

        val graviksInstance = GraviksInstance(
            boiler, maxNumDescriptorImages = 2
        )

        val graviks = GraviksContext(graviksInstance, 20, 20, initialBackgroundColor = backgroundColor)

        val colors = (0 until 20).map { Color.rgbInt(10 * it, 5 * it, it) }
        val images = colors.map { color ->
            val bufferedImage = BufferedImage(1, 1, TYPE_INT_ARGB)
            bufferedImage.setRGB(0, 0, java.awt.Color(color.red, color.green, color.blue).rgb)

            val file = Files.createTempFile(null, ".png").toFile()
            ImageIO.write(bufferedImage, "PNG", file)

            ImageReference.file(file)
        }

        withTestImage(graviks, true) { update, hostImage ->
            for ((index, image) in images.withIndex()) {
                graviks.drawImage(index / 20f, index / 20f, 1f, 1f, image)
            }
            update()
            for ((index, color) in colors.withIndex()) {
                for (x in index until 20) {
                    assertColorEquals(color, hostImage.getPixel(x, index))
                }
                for (y in index until 20) {
                    assertColorEquals(color, hostImage.getPixel(index, y))
                }
            }
        }

        graviks.destroy()
        graviksInstance.destroy()
    }

    @Test
    fun testGetImageSize() {
        val graviks = GraviksContext(
            this.graviksInstance, 50, 50,
            initialBackgroundColor = Color.rgbInt(1, 2, 3)
        )

        assertEquals(
            Pair(14, 14),
            graviks.getImageSize(ImageReference.classLoaderPath("graviks2d/images/test1.png", false))
        )

        val testFile = Files.createTempFile("", ".png").toFile()
        val testImage = BufferedImage(10, 11, TYPE_INT_ARGB)
        ImageIO.write(testImage, "PNG", testFile)

        assertEquals(Pair(10, 11), graviks.getImageSize(ImageReference.file(testFile)))

        graviks.destroy()
    }

    @Test
    fun testGetStringAspectRatio() {
        val graviks = GraviksContext(
            this.graviksInstance, 50, 50,
            initialBackgroundColor = Color.rgbInt(1, 2, 3)
        )

        val aspectHelloWorld = graviks.getStringAspectRatio("Hello, World!", null)
        assertEquals(5.11f, aspectHelloWorld, 0.5f)
        val aspectH = graviks.getStringAspectRatio("H", null)
        assertEquals(0.58f, aspectH, 0.2f)
        assertEquals(0f, graviks.getStringAspectRatio("", null))

        graviks.destroy()
    }

    @Test
    fun testAutomaticDepth() {
        val colors = (0 until 10).map { Color.rgbInt(10 * it, 10 * it, 10 * it) }
        val graviks = GraviksContext(this.graviksInstance, 1, 1)

        withTestImage(graviks, true) { update, hostImage ->
            for (color in colors) {
                graviks.fillRect(0f, 0f, 1f, 1f, color)
                update()
                assertColorEquals(color, hostImage.getPixel(0, 0))
            }

            for (color in colors) {
                graviks.fillRect(0f, 0f, 1f, 1f, color)
            }
            update()
            assertColorEquals(colors[colors.size - 1], hostImage.getPixel(0, 0))
        }

        graviks.destroy()
    }

    @Test
    fun testBlitColorImageTo() {
        val graviks = GraviksContext(
            this.graviksInstance, 50, 50,
            initialBackgroundColor = Color.rgbInt(10, 20, 30)
        )

        stackPush().use { stack ->
            val destImage = boiler.images.create(
                stack, graviks.width, graviks.height, VK_FORMAT_B8G8R8A8_SRGB,
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT or VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                VK_IMAGE_ASPECT_COLOR_BIT, VK_SAMPLE_COUNT_1_BIT, 1, 1,
                false, "TestDestImage"
            )

            graviks.copyColorImageTo(
                destImage = destImage.vkImage, destBuffer = null, destImageFormat = VK_FORMAT_B8G8R8A8_SRGB,
                originalImageLayout = VK_IMAGE_LAYOUT_UNDEFINED, finalImageLayout = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                imageSrcUsage = null, imageDstUsage = ResourceUsage(VK_ACCESS_TRANSFER_READ_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT),
                shouldAwaitCompletion = true
            )

            val destBuffer = boiler.buffers.createMapped(
                graviks.width * graviks.height * 4L, VK_BUFFER_USAGE_TRANSFER_DST_BIT, "TestDestBuffer"
            )

            val destHostBuffer = memByteBuffer(destBuffer.hostAddress, graviks.width * graviks.height * 4)

            val commandPool = boiler.commands.createPool(
                VK_COMMAND_POOL_CREATE_TRANSIENT_BIT, boiler.queueFamilies().graphics.index, "TestCopy"
            )
            val commandBuffer = boiler.commands.createPrimaryBuffers(commandPool, 1, "TestCopy")[0]
            val recorder = CommandRecorder.begin(commandBuffer, boiler, stack, "TestGraviksContextCopy")
            recorder.copyImageToBuffer(
                VK_IMAGE_ASPECT_COLOR_BIT, destImage.vkImage,
                graviks.width, graviks.height, destBuffer.vkBuffer
            )
            recorder.end()

            val copyFence = boiler.sync.createFences(false, 1, "TestGraviksContextCopyFence")[0]
            boiler.queueFamilies().graphics.queues.random().submit(
                commandBuffer, "TestContext.testBlitColorImage", emptyArray(), copyFence
            )
            assertVkSuccess(
                vkWaitForFences(boiler.vkDevice(), stack.longs(copyFence), true, 1_000_000_000),
                "vkWaitForFences", "TestGraviksContextCopy"
            )

            vkDestroyFence(boiler.vkDevice(), copyFence, null)
            vkDestroyCommandPool(boiler.vkDevice(), commandPool, null)

            for (x in 0 until graviks.width) {
                for (y in 0 until graviks.height) {
                    val index = 4 * (x + y * graviks.width)
                    assertEquals(30, destHostBuffer[index].toUByte().toInt())
                    assertEquals(20, destHostBuffer[index + 1].toUByte().toInt())
                    assertEquals(10, destHostBuffer[index + 2].toUByte().toInt())
                    assertEquals(255, destHostBuffer[index + 3].toUByte().toInt())
                }
            }

            vmaDestroyImage(boiler.vmaAllocator(), destImage.vkImage, destImage.vmaAllocation)
            vmaDestroyBuffer(boiler.vmaAllocator(), destBuffer.vkBuffer, destBuffer.vmaAllocation)
        }

        graviks.destroy()
    }

    @AfterAll
    fun destroyGraviksInstance() {
        this.graviksInstance.destroy()
        this.boiler.destroyInitialObjects()
    }
}
