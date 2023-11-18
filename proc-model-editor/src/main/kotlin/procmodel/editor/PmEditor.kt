package procmodel.editor

import com.github.knokko.boiler.instance.BoilerInstance
import graviks2d.resource.image.ImageReference
import graviks2d.resource.text.TextStyle
import graviks2d.util.Color
import gruviks.component.Component
import gruviks.component.fill.SimpleColorFillComponent
import gruviks.component.menu.SimpleFlatMenu
import gruviks.component.menu.controller.SimpleFlatController
import gruviks.component.menu.controller.SimpleListViewController
import gruviks.component.menu.controller.TreeViewController
import gruviks.component.text.TextButton
import gruviks.component.text.TextComponent
import gruviks.component.util.SwitchComponent
import gruviks.event.Event
import gruviks.event.RemoveEvent
import gruviks.space.Coordinate
import gruviks.space.Point
import gruviks.space.RectRegion
import gruviks.space.SpaceLayout
import org.lwjgl.vulkan.VK10.vkDeviceWaitIdle
import procmodel.lang.types.PmValue
import procmodel.renderer.PmInstance

private class SaveOnQuitController<Vertex, VertexValue : PmValue, Matrix>(
    val openFiles: List<OpenFile<Vertex, VertexValue, Matrix>>,
    val boiler: BoilerInstance, val pmInstance: PmInstance<Vertex, Matrix>
): SimpleFlatController(true) {
    override fun processEvent(event: Event) {
        if (event is RemoveEvent) {
            for (openFile in openFiles) openFile.save(true)
            vkDeviceWaitIdle(boiler.vkDevice())
            pmInstance.destroy()
        }
    }
}

private val directoryIcon = ImageReference.classLoaderPath("procmodel/editor/directory.png", false)
private val fileIcon = ImageReference.classLoaderPath("procmodel/editor/file.png", false)
private val parametersIcon = ImageReference.classLoaderPath("procmodel/editor/parameters.png", false)
private val trianglesIcon = ImageReference.classLoaderPath("procmodel/editor/triangles.png", false)

fun <Vertex, VertexValue : PmValue, Matrix> createPmEditor(
    boiler: BoilerInstance, config: EditorConfig<Vertex, VertexValue, Matrix>
): Component {
    val pm2Instance = PmInstance(boiler, config.modelInfo)
    val rootMenu = SimpleFlatMenu(SpaceLayout.Simple, backgroundColor.scale(1.2f))

    val openFiles = mutableListOf<OpenFile<Vertex, VertexValue, Matrix>>()
    val fileTree = TreeViewController.Node(config.rootDirectory, null)

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
                        config, node.element, openFiles, content, errorComponent,
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
