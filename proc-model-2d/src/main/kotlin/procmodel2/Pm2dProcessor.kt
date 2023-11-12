package procmodel2

import procmodel.lang.types.PmValue
import procmodel.model.PmModel
import procmodel.processor.PmVertexProcessor
import procmodel.program.PmProgram

object Pm2dProcessor {
    fun execute(program: PmProgram, staticParameters: PmValue): PmModel<Pm2dVertex> {
        val processor = PmVertexProcessor(program, staticParameters, Pm2dTypes.vertex, Pm2dVertexValue::toVertex)
        for ((name, function) in Pm2dBuiltinFunctions.all) {
            processor.addBuiltinFunction(name, function)
        }
        return processor.execute()
    }

    fun loadStaticParametersFromClassPath(prefix: String, path: String): PmValue {
        val importer = Pm2dCompiler.createClassPathIporter(prefix)
        return importer.importValue("/$path", false, Pm2dCompiler.extraFunctions, Pm2dTypes.all)
    }
}
