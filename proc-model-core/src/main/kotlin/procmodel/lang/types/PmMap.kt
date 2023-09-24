package procmodel.lang.types

import procmodel.exceptions.PmRuntimeError

class PmMap: PmValue() {

    val map = mutableMapOf<PmValue, PmValue>()

    override fun copy(): PmValue {
        val copy = PmMap()
        for ((key, value) in this.map) {
            copy.map[key.copy()] = value.copy()
        }
        return copy
    }

    override fun toString() = "PmMap(${map.size} entries)"

    override operator fun get(key: PmValue) = map[key] ?: throw PmRuntimeError("Map $map doesn't have key $key")

    override operator fun set(key: PmValue, value: PmValue) {
        map[key] = value
    }

    override fun equals(other: Any?) = other is PmMap && this.map == other.map

    override fun hashCode() = this.map.hashCode()
}
