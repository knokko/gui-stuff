package procmodel.importer

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

object PmShapes {
    fun <VertexValue> write(
        vertexSet: Set<VertexValue>,
        triangleList: List<VertexValue>,
        output: OutputStream,
        writeVertex: (VertexValue, MutableMap<String, Any>) -> Unit
    ) {
        val vertexList = vertexSet.toList()
        val vertexMap = mutableMapOf<VertexValue, Int>()
        for ((index, vertex) in vertexList.withIndex()) {
            vertexMap[vertex] = index
        }

        val verticesToWrite = vertexList.map { vertexValue ->
            val map = mutableMapOf<String, Any>()
            writeVertex(vertexValue, map)
            map
        }

        val trianglesToWrite = triangleList.map { vertexValue ->
            val index = vertexMap[vertexValue] ?: throw IllegalArgumentException("Vertex $vertexValue is not in $vertexSet")
            index
        }

        val objectOutput = ObjectOutputStream(output)
        objectOutput.writeObject(verticesToWrite)
        objectOutput.writeObject(trianglesToWrite)
        objectOutput.flush()
    }

    @Suppress("UNCHECKED_CAST")
    fun <VertexValue> parse(
        input: InputStream, parseVertex: (Map<String, Any>) -> VertexValue
    ): Pair<Set<VertexValue>, List<VertexValue>> {
        val objectInput = ObjectInputStream(input)
        val verticesToWrite = objectInput.readObject() as List<Map<String, Any>>
        val trianglesToWrite = objectInput.readObject() as List<Int>

        val vertexList = verticesToWrite.map(parseVertex)
        val vertexSet = vertexList.toSet()

        val triangleList = trianglesToWrite.map { index -> vertexList[index] }
        return Pair(vertexSet, triangleList)
    }
}
