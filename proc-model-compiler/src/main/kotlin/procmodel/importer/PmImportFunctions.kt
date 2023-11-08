package procmodel.importer

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.*

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

class PmClassPathImportFunctions(private val prefix: String): PmImportFunctions {
    override fun getSourceCode(path: String): String? {
        val inputStream = PmClassPathImportFunctions::class.java.classLoader.getResourceAsStream(prefix + path)
            ?: return null

        val sourceCode = StringBuilder()
        val scanner = Scanner(inputStream)
        while (scanner.hasNextLine()) {
            sourceCode.appendLine(scanner.nextLine())
        }
        scanner.close()

        return sourceCode.toString()
    }

    override fun getBinaryInput(path: String): InputStream? {
        return PmClassPathImportFunctions::class.java.classLoader.getResourceAsStream(prefix + path) ?: return null
    }
}
