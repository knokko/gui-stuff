package procmodel.editor2d

import com.github.knokko.boiler.builder.BoilerBuilder
import com.github.knokko.boiler.builder.instance.ValidationFeatures
import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.profiler.SampleProfiler
import com.github.knokko.profiler.storage.SampleStorage
import graviks.glfw.GraviksWindow
import graviks2d.context.GraviksContext
import gruviks.glfw.createAndControlGruviksWindow
import org.joml.Matrix3x2f
import org.lwjgl.vulkan.KHRDynamicRendering.VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME
import org.lwjgl.vulkan.VK12.*
import org.lwjgl.vulkan.VkPhysicalDeviceDynamicRenderingFeaturesKHR
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2
import procmodel.editor.EditorConfig
import procmodel.editor.PmColorTheme
import procmodel.editor.createPmEditor
import procmodel.editor.pmSyntaxTextAreaStyle
import procmodel.importer.PmImporter
import procmodel.lang.types.PmValue
import procmodel.model.PmModel
import procmodel.program.PmProgram
import procmodel.renderer.PmInstance
import procmodel2.*
import java.io.File
import java.io.PrintWriter

private class Pm2dEditorConfig(boiler: BoilerInstance) : EditorConfig<Pm2dVertex, Pm2dVertexValue, Matrix3x2f>(
    rootDirectory = File("pm2-models/"),
    modelExtension = "pm2",
    valueExtension = "pv2",
    trianglesExtension = "tri2",
    modelInfo = createModelInfo2d(boiler),
    textAreaStyle = pmSyntaxTextAreaStyle(PmColorTheme.TEST, 0.05f) { allLines, colorTheme ->
        Pm2dSyntaxHighlighter(allLines, colorTheme)
    }
) {
    override fun parseVertex(attributes: Map<String, Any>) = Pm2dShapes.parseVertex(attributes)

    override fun compute(program: PmProgram) = Pm2dProcessor.compute(program)

    override fun execute(program: PmProgram, staticParameters: PmValue) = Pm2dProcessor.execute(program, staticParameters)

    override fun createPreview(
        boiler: BoilerInstance,
        pmInstance: PmInstance<Pm2dVertex, Matrix3x2f>,
        initialModel: PmModel<Pm2dVertex>?,
        reportError: (String) -> Unit
    ) = Pm2PreviewComponent(boiler, pmInstance, initialModel, reportError)

    override fun createShapeEditor(vertices: Set<Pm2dVertexValue>, triangles: List<Pm2dVertexValue>) = Pm2dShapeEditor(
        vertices.toMutableSet(), triangles.toMutableList()
    )

    override fun compile(sourceCode: String, importer: PmImporter<Pm2dVertexValue>) =
        Pm2dCompiler.compile(sourceCode, importer)

    override fun writeVertex(vertex: Pm2dVertexValue, outAttributes: MutableMap<String, Any>) =
        Pm2dShapes.writeVertex(vertex, outAttributes)

}

fun main() {
    val storage = SampleStorage.frequency()
    val profiler = SampleProfiler(storage)
    profiler.start()

    val builder = BoilerBuilder(VK_API_VERSION_1_2, "Pm2Editor", 1)
        .validation(ValidationFeatures(false, false, false, true, true))
        .requiredDeviceExtensions(setOf(VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME))
        .extraDeviceRequirements { physicalDevice, _, stack ->
            val dynamicRendering = VkPhysicalDeviceDynamicRenderingFeaturesKHR.calloc(stack)
            dynamicRendering.`sType$Default`()

            val features2 = VkPhysicalDeviceFeatures2.calloc(stack)
            features2.`sType$Default`()
            features2.pNext(dynamicRendering)

            vkGetPhysicalDeviceFeatures2(physicalDevice, features2)

            dynamicRendering.dynamicRendering()
        }
        .beforeDeviceCreation { ciDevice, _, stack ->
            val dynamicRendering = VkPhysicalDeviceDynamicRenderingFeaturesKHR.calloc(stack)
            dynamicRendering.`sType$Default`()
            dynamicRendering.dynamicRendering(true)

            ciDevice.pNext(dynamicRendering)
        }

    val graviksWindow = GraviksWindow(
        1600, 900, builder
    ) { instance, width, height -> GraviksContext(instance, width, height) }

    createAndControlGruviksWindow(graviksWindow, createPmEditor(graviksWindow.boiler, Pm2dEditorConfig(graviksWindow.boiler)))
    profiler.stop()
    storage.getThreadStorage(Thread.currentThread().threadId()).print(
        PrintWriter(File("dsl-profiler.log")), 10, 0.1
    )
}
