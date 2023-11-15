package procmodel2

import procmodel.exceptions.PmRuntimeError
import procmodel.lang.types.PmValue
import procmodel.model.PmModel
import procmodel.processor.PmValueProcessor
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

    fun compute(program: PmProgram): PmValue {
        val processor = PmValueProcessor(program.body)
        for ((name, function) in Pm2dBuiltinFunctions.all) {
            processor.addBuiltinFunction(name, function)
        }
        processor.execute()
        return processor.result ?: throw PmRuntimeError("No result was produced")
    }

    fun loadStaticParametersFromClassPath(prefix: String, path: String): PmValue {
        val importer = Pm2dCompiler.createClassPathImporter(prefix)
        return importer.importValue("/$path", false, Pm2dCompiler.extraFunctions, Pm2dTypes.all)
    }
}
