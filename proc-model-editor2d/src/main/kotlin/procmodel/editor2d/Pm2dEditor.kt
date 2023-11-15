package procmodel.editor2d

import com.github.knokko.boiler.instance.BoilerInstance
import com.github.knokko.profiler.SampleProfiler
import com.github.knokko.profiler.storage.SampleStorage
import graviks.glfw.GraviksWindow
import graviks2d.context.GraviksContext
import graviks2d.resource.image.ImageReference
import graviks2d.resource.text.TextStyle
import graviks2d.util.Color
import gruviks.component.Component
import gruviks.component.fill.SimpleColorFillComponent
import gruviks.component.menu.SimpleFlatMenu
import gruviks.component.menu.controller.SimpleFlatController
import gruviks.component.menu.controller.SimpleListViewController
import gruviks.component.menu.controller.TreeViewController
import gruviks.component.text.*
import gruviks.component.util.SwitchComponent
import gruviks.event.Event
import gruviks.event.RemoveEvent
import gruviks.glfw.createAndControlGruviksWindow
import gruviks.space.Coordinate
import gruviks.space.Point
import gruviks.space.RectRegion
import gruviks.space.SpaceLayout
import org.joml.Matrix3x2f
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3
import procmodel.renderer.PmInstance
import procmodel2.Pm2dVertex
import procmodel2.createModelInfo2d
import java.io.File
import java.io.PrintWriter

internal val rootDirectory = File("proc-model-test-case/src/main/resources/procmodel/test/")

private class SaveOnQuitController(
    val openFiles: List<OpenFile>, val boiler: BoilerInstance, val pmInstance: PmInstance<Pm2dVertex, Matrix3x2f>
): SimpleFlatController(true) {
    override fun processEvent(event: Event) {
        if (event is RemoveEvent) {
            for (openFile in openFiles) openFile.save(true)
            vkDeviceWaitIdle(boiler.vkDevice())
            pmInstance.destroy()
        }
    }
}

private val directoryIcon = ImageReference.classLoaderPath("procmodel/editor2d/directory.png", false)
private val fileIcon = ImageReference.classLoaderPath("procmodel/editor2d/file.png", false)
private val parametersIcon = ImageReference.classLoaderPath("procmodel/editor2d/parameters.png", false)
private val trianglesIcon = ImageReference.classLoaderPath("procmodel/editor2d/triangles.png", false)

fun createPm2Editor(boiler: BoilerInstance): Component {
    val pm2Instance = PmInstance(boiler, createModelInfo2d(boiler))
    val rootMenu = SimpleFlatMenu(SpaceLayout.Simple, backgroundColor.scale(1.2f))

    val openFiles = mutableListOf<OpenFile>()
    val fileTree = TreeViewController.Node(rootDirectory, null)

    val errorComponent = TextComponent("", TextStyle(fillColor = Color.RED, font = null))
    val contentBackground = SimpleColorFillComponent(backgroundColor)
    val content = SwitchComponent(contentBackground)
    val upperBar = SimpleFlatMenu(SpaceLayout.GrowRight, backgroundColor.scale(1.3f))

    val fileTabsController = SimpleListViewController(openFiles) { element, _, position, components, refreshController ->
        val componentPosition = position ?: Point.percentage(0, 0)
        val region = RectRegion(
            componentPosition.x, Coordinate.percentage(0),
            componentPosition.x + Coordinate.percentage(480), Coordinate.percentage(100)
        )

        val fileName = (element.parametersFile ?: element.modelFile).name
        val extensionIndex = fileName.lastIndexOf('.')
        val simpleFileName = fileName.substring(0 until extensionIndex)
        components.add(Pair(TextButton(simpleFileName, null, tabStyle) { event, _ ->
            if (event.button == 0) {
                content.setComponent(element.getPrimaryComponent())
            } else if (event.button == 1) {
                if (element.save(false)) {
                    content.setComponent(contentBackground)
                    content.removeComponent(element.getPrimaryComponent())
                    if (element.preview.isInitialized()) {
                        val preview = element.preview.value
                        if (preview != null) content.removeComponent(preview.first)
                    }
                    openFiles.remove(element)
                    refreshController()
                } else errorComponent.setText("Saving failed due to an IOException")
            } else if (event.button == 2) {
                val preview = element.preview.value
                if (preview != null) {
                    content.setComponent(preview.first)
                    element.updatePreview(errorComponent, openFiles)
                }
            }
        }, region))

        Point(region.boundX, Coordinate.percentage(0))
    }
    val fileTabs = SimpleFlatMenu(SpaceLayout.GrowRight, backgroundColor.scale(1.2f))
    fileTabs.addController(fileTabsController)

    val fileTreeController = TreeViewController(
        fileTree,
        { parent -> Point(parent.x + Coordinate.percentage(20), parent.y) },
        { child -> Point(child.x - Coordinate.percentage(20), child.y) },
        { node, indices, position, components, refreshController ->
            val componentPosition = position ?: Point.percentage(0, 100)
            val region = RectRegion(
                componentPosition.x, componentPosition.y - Coordinate.percentage(12),
                componentPosition.x + Coordinate.percentage(80), componentPosition.y
            )

            val fileName = node.element.name

            if (node.element.isDirectory) {
                components.add(Pair(TextButton(fileName, directoryIcon, fileButtonStyle) { _, _ ->
                    val files = node.element.listFiles()
                    if (files == null) {
                        // Edge case: the directory has been removed, or is no longer a directory
                        val parent = fileTree.getChild(indices.subList(0, indices.size - 1))
                        parent.children = null
                        refreshController()
                    } else {
                        if (node.children == null) {
                            val directories = files.filter { it.isDirectory }
                            val modelFiles = files.filter { !it.isDirectory && it.name.endsWith(".pm2") }
                            val valueFiles = files.filter { !it.isDirectory && it.name.endsWith(".pv2") }
                            val triangleFiles = files.filter { !it.isDirectory && it.name.endsWith(".tri2") }
                            node.children = (directories + modelFiles + valueFiles + triangleFiles).map {
                                TreeViewController.Node(it, null)
                            }.toMutableList()
                        } else node.children = null
                        refreshController()
                    }
                }, region))
            } else if (fileName.endsWith(".pm2") || fileName.endsWith(".pv2") || fileName.endsWith(".tri2")) {
                val icon = if (fileName.endsWith(".pm2")) fileIcon else if (fileName.endsWith(".pv2")) parametersIcon else trianglesIcon
                val extensionIndex = fileName.lastIndexOf('.')
                val simpleName = fileName.substring(0, extensionIndex)
                components.add(Pair(TextButton(simpleName, icon, fileButtonStyle) { _, _ ->
                    OpenFile.open(
                        node.element, openFiles, content, errorComponent,
                        fileTabsController::refresh, boiler, pm2Instance
                    )
                }, region))
            }

            Point(region.minX, region.minY)
        }
    )
    val fileTreeMenu = SimpleFlatMenu(SpaceLayout.GrowDown, backgroundColor.scale(1.4f))
    fileTreeMenu.addController(fileTreeController)

    rootMenu.addComponent(upperBar, RectRegion.percentage(0, 95, 100, 100))
    rootMenu.addComponent(fileTreeMenu, RectRegion.percentage(0, 0, 20, 95))
    rootMenu.addComponent(fileTabs, RectRegion.percentage(20, 90, 100, 95))
    rootMenu.addComponent(content, RectRegion.percentage(20, 5, 100, 90))
    rootMenu.addComponent(errorComponent, RectRegion.percentage(20, 0, 100, 5))

    rootMenu.addController(SaveOnQuitController(openFiles, boiler, pm2Instance))

    return rootMenu
}

fun main() {
    val storage = SampleStorage.frequency()
    val profiler = SampleProfiler(storage)
    profiler.start()
    // TODO Find a less dirty way to configure the boiler of GraviksWindow
    val graviksWindow = GraviksWindow(
        1600, 900, true,
        "Pm2Editor", VK_MAKE_VERSION(0, 1, 0), true,
        { builder -> builder.featurePicker13 { _, _, toEnable -> toEnable.dynamicRendering(true) } }, VK_API_VERSION_1_3
    ) { instance, width, height -> GraviksContext(instance, width, height) }

    createAndControlGruviksWindow(graviksWindow, createPm2Editor(graviksWindow.boiler))
    profiler.stop()
    storage.getThreadStorage(Thread.currentThread().threadId()).print(
        PrintWriter(File("dsl-profiler.log")), 10, 0.1
    )
}
