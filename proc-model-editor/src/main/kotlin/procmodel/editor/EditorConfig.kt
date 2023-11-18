package procmodel.editor

import com.github.knokko.boiler.instance.BoilerInstance
import gruviks.component.text.TextAreaStyle
import procmodel.importer.PmImporter
import procmodel.lang.types.PmValue
import procmodel.model.PmModel
import procmodel.program.PmProgram
import procmodel.renderer.PmInstance
import procmodel.renderer.config.PmModelInfo
import java.io.File

abstract class EditorConfig<Vertex, VertexValue: PmValue, Matrix>(
    val rootDirectory: File,
    val modelExtension: String,
    val valueExtension: String,
    val trianglesExtension: String,
    val modelInfo: PmModelInfo<Vertex, Matrix>,
    val textAreaStyle: TextAreaStyle
) {
    abstract fun parseVertex(attributes: Map<String, Any>): VertexValue

    abstract fun writeVertex(vertex: VertexValue, outAttributes: MutableMap<String, Any>)

    abstract fun compile(sourceCode: String, importer: PmImporter<VertexValue>): PmProgram

    abstract fun compute(program: PmProgram): PmValue

    abstract fun execute(program: PmProgram, staticParameters: PmValue): PmModel<Vertex>

    abstract fun createShapeEditor(vertices: Set<VertexValue>, triangles: List<VertexValue>): PmShapeEditor<VertexValue>

    abstract fun createPreview(
        boiler: BoilerInstance, pmInstance: PmInstance<Vertex, Matrix>,
        initialModel: PmModel<Vertex>?, reportError: (String) -> Unit
    ): PmPreviewComponent<Vertex>
}
