package procmodel.lang.types

import procmodel.lang.types.hints.PmTypeHint
import java.util.*

/**
 * Represents a `PmType` with an associated *hint*. The *hint* is completely optional, and may specify hints to the
 * config UI (e.g. use a slider with a minimum value of 5 and a maximum value of 40).
 */
class PmFatType(
    val type: PmType,
    val hint: PmTypeHint?
) {
    override fun equals(other: Any?) = other is PmFatType && this.type == other.type && this.hint == other.hint

    override fun hashCode() = type.hashCode() + 13 * Objects.hashCode(hint)

    override fun toString() = "PmFatType($type, hint=$hint)"
}
