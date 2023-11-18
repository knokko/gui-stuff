package procmodel.editor

import com.github.knokko.boiler.instance.BoilerInstance
import gruviks.component.Component
import gruviks.component.text.TextArea
import gruviks.component.text.TextComponent
import gruviks.component.util.SwitchComponent
import procmodel.exceptions.PmCompileError
import procmodel.exceptions.PmRuntimeError
import procmodel.importer.PmFileImportFunctions
import procmodel.importer.PmImportCache
import procmodel.importer.PmImporter
import procmodel.importer.PmShapes
import procmodel.lang.types.PmNone
import procmodel.lang.types.PmValue
import procmodel.model.PmModel
import procmodel.renderer.PmInstance
import java.io.File
import java.io.IOException
import java.nio.file.Files

private fun <Vertex, VertexValue : PmValue, Matrix> createImporter(
    config: EditorConfig<Vertex, VertexValue, Matrix>,
    sourceFile: File, openFiles: List<OpenFile<Vertex, VertexValue, Matrix>>
): PmImporter<VertexValue> {
    for (openFile in openFiles) openFile.save(false)

    val rootPath = config.rootDirectory.absolutePath
    val sourcePath = sourceFile.absoluteFile.parentFile.absolutePath
    if (!sourcePath.startsWith(rootPath)) {
        throw Error("The source $sourcePath is not a descendant from the root $rootPath")
    }

    var prefix = sourcePath.substring(rootPath.length)
    if (prefix.startsWith("/") || prefix.startsWith("\\")) prefix = prefix.substring(1)
    return PmImporter(PmImportCache(PmFileImportFunctions(config.rootDirectory)), prefix, config::parseVertex)
}

interface PmShapeEditor<VertexValue : PmValue> {
    val points: Set<VertexValue>
    val triangles: List<VertexValue>
}

class OpenFile<Vertex, VertexValue : PmValue, Matrix>(
    private val config: EditorConfig<Vertex, VertexValue, Matrix>,
    val modelFile: File,
    val parametersFile: File?,
    private val textArea: TextArea?,
    private val shapeEditor: PmShapeEditor<VertexValue>?,
    private val createPreview: () -> Pair<Component, (PmModel<Vertex>) -> Unit>?
) {
    val preview = lazy { createPreview() }

    fun getPrimaryComponent(): Component {
        if (textArea != null) return textArea
        return shapeEditor as Component
    }

    fun updatePreview(errorComponent: TextComponent, openFiles: List<OpenFile<Vertex, VertexValue, Matrix>>) {
        // Shape files are the only files without textArea, and also the only files without preview
        if (textArea == null) return

        val modelContent = if (parametersFile == null) textArea.getText() else try {
            val openFile = openFiles.find { it.parametersFile == null && it.modelFile == this.modelFile }
            openFile?.textArea?.getText() ?: Files.readString(modelFile.toPath())
        } catch (failed: Throwable) {
            errorComponent.setText("Failed to open model file: " + failed.message)
            return
        }

        val parameters = if (parametersFile != null) {
            try {
                val importer = createImporter(config, parametersFile, openFiles)
                config.compute(config.compile(textArea.getText(), importer))
            } catch (compileError: PmCompileError) {
                errorComponent.setText(compileError.message ?: "Failed to compile parameters")
                compileError.printStackTrace()
                return
            } catch (runtimeError: PmRuntimeError) {
                errorComponent.setText(runtimeError.message ?: "Failed to run parameters")
                return
            }
        } else PmNone()

        try {
            val startTime = System.currentTimeMillis()
            val importer = createImporter(config, modelFile, openFiles)
            val newProgram = config.compile(modelContent, importer)
            val time1 = System.currentTimeMillis()
            val newModel = config.execute(newProgram, parameters)
            val time2 = System.currentTimeMillis()

            println("Compilation took ${time1 - startTime} ms and running took ${time2 - time1} ms")
            preview.value!!.second(newModel)
            errorComponent.setText("")
        } catch (compileError: PmCompileError) {
            errorComponent.setText(compileError.message!!)
        } catch (runtimeError: PmRuntimeError) {
            errorComponent.setText(runtimeError.message!!)
        }
    }

    fun save(handleErrors: Boolean): Boolean {
        if (textArea != null) {
            val content = textArea.getText()
            val file = parametersFile ?: modelFile
            return try {
                Files.writeString(file.toPath(), content)
                true
            } catch (failedToSave: IOException) {
                println("Failed to save $file: ${failedToSave.message}")
                if (handleErrors) {
                    println("Content should be this, good luck with it:")
                    println("--------------")
                    println(content)
                    println("--------------")
                }
                false
            }
        } else {
            return try {
                val output = Files.newOutputStream(modelFile.toPath())
                PmShapes.write(shapeEditor!!.points, shapeEditor.triangles, output, config::writeVertex)
                output.close()
                true
            } catch (failedToSave: IOException) {
                println("Failed to save $modelFile: ${failedToSave.message}")
                false
            }
        }
    }

    companion object {
        fun <Vertex, VertexValue : PmValue, Matrix> open(
            config: EditorConfig<Vertex, VertexValue, Matrix>,
            file: File, openFiles: MutableList<OpenFile<Vertex, VertexValue, Matrix>>,
            switchComponent: SwitchComponent, errorComponent: TextComponent, updateController: () -> Unit,
            boiler: BoilerInstance, pm2Instance: PmInstance<Vertex, Matrix>
        ) {
            val modelFile: File
            val parametersFile: File?
            if (file.name.endsWith(config.modelExtension)) {
                modelFile = file
                parametersFile = null
            } else if (file.name.endsWith(config.valueExtension)) {
                val currentDirectory = file.parentFile
                if (currentDirectory == null) {
                    errorComponent.setText("Can't find directory containing this file")
                    return
                }

                val parentDirectory = currentDirectory.parentFile
                if (parentDirectory == null) {
                    errorComponent.setText("Can't find parent directory of this file")
                    return
                }

                modelFile = File("$parentDirectory/${currentDirectory.name}.${config.modelExtension}")
                parametersFile = file
            } else if (file.name.endsWith(config.trianglesExtension)) {
                modelFile = file
                parametersFile = null
            } else {
                errorComponent.setText("Unexpected file type: $file")
                return
            }

            var openFile = openFiles.find { it.modelFile == modelFile && it.parametersFile == parametersFile }
            if (openFile != null) openFiles.remove(openFile)
            if (openFile == null) {
                if (file.name.endsWith(config.modelExtension) || file.name.endsWith(config.valueExtension)) {
                    val modelContent: String
                    val parameterContent: String?
                    try {
                        modelContent = Files.readString(modelFile.toPath())
                        parameterContent = if (parametersFile != null) Files.readString(parametersFile.toPath())
                        else null
                    } catch (failedToOpen: Throwable) {
                        errorComponent.setText(failedToOpen.message ?: "Failed to open file")
                        failedToOpen.printStackTrace()
                        return
                    }

                    val initialParameters = if (parameterContent != null) {
                        try {
                            config.compute(config.compile(parameterContent, createImporter(config, parametersFile!!, openFiles)))
                        } catch (compileError: PmCompileError) {
                            compileError.printStackTrace()
                            errorComponent.setText(compileError.message ?: "Parameter compile error")
                            PmNone()
                        } catch (runtimeError: PmRuntimeError) {
                            errorComponent.setText(runtimeError.message ?: "Parameter runtime error")
                            PmNone()
                        }
                    } else PmNone()

                    val initialModel = try {
                        config.execute(
                            config.compile(modelContent, createImporter(config, modelFile, openFiles)),
                            initialParameters
                        )
                    } catch (compileError: PmCompileError) {
                        compileError.printStackTrace()
                        null
                    } catch (runtimeError: PmRuntimeError) {
                        null
                    }

                    val textArea = TextArea(parameterContent ?: modelContent, config.textAreaStyle, null)
                    val createPreview = {
                        val previewComponent = config.createPreview(
                            boiler, pm2Instance, initialModel, errorComponent::setText
                        )
                        val configurationComponent = createParameterConfigurationMenu(previewComponent)
                        Pair(configurationComponent, previewComponent::updateModel)
                    }
                    openFile = OpenFile(config, modelFile, parametersFile, textArea, null, createPreview)
                } else if (file.name.endsWith(config.trianglesExtension)) {
                    val shapeEditor = if (file.length() == 0L) {
                        config.createShapeEditor(mutableSetOf(), mutableListOf())
                    } else {
                        try {
                            val input = Files.newInputStream(file.toPath())
                            val (vertexSet, triangleList) = PmShapes.parse(input, config::parseVertex)
                            input.close()
                            config.createShapeEditor(vertexSet.toMutableSet(), triangleList.toMutableList())
                        } catch (failed: Throwable) {
                            failed.printStackTrace()
                            errorComponent.setText("Failed to open triangle file: ${failed.message}")
                            return
                        }
                    }

                    openFile = OpenFile(config, modelFile, null, null, shapeEditor) { null }
                } else {
                    throw IllegalArgumentException("Unexpected file $file")
                }
            }

            openFiles.add(0, openFile)
            switchComponent.setComponent(openFile.getPrimaryComponent())
            updateController()
        }
    }
}
