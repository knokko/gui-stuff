package procmodel2

import graviks2d.util.Color
import procmodel.lang.types.PmColor

object Pm2dShapes {

    fun parseVertex(map: Map<String, Any>): Pm2dVertexValue {
        val x = map["x"] as Float
        val y = map["y"] as Float
        val rawColor = map["color"]

        val vertex = Pm2dVertexValue()
        vertex.setProperty("position", Pm2dPositionValue(x, y))
        if (rawColor != null) vertex.setProperty("color", PmColor(Color.raw(rawColor as Int)))

        return vertex
    }

    fun writeVertex(vertexValue: Pm2dVertexValue, map: MutableMap<String, Any>) {
        val vertex = vertexValue.toVertex()
        map["x"] = vertex.x
        map["y"] = vertex.y
        map["color"] = vertex.color.rawValue
    }
}
