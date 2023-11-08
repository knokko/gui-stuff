package procmodel.importer

import java.io.File
import java.io.InputStream
import java.nio.file.Files

interface PmImportFunctions {

    fun getSourceCode(path: String): String?

    fun getBinaryInput(path: String): InputStream?
}

class PmDummyImportFunctions: PmImportFunctions {

    override fun getSourceCode(path: String): String? = null

    override fun getBinaryInput(path: String): InputStream? = null
}

class PmFileImportFunctions(private val root: File): PmImportFunctions {

    override fun getSourceCode(path: String): String? {
        val file = File("$root/$path")
        return try {
            Files.readString(file.toPath())
        } catch (failed: Throwable) { null }
    }

    override fun getBinaryInput(path: String): InputStream? {
        val file = File("$root/$path")
        return try {
            Files.newInputStream(file.toPath())
        } catch (failed: Throwable) { null }
    }
}
