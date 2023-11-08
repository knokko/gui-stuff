package procmodel.test

import org.joml.Matrix3x2f
import procmodel.lang.types.PmValue

class Matrix(val inner: Matrix3x2f) : PmValue() {

    override fun copy() = Matrix(Matrix3x2f(inner))

    override fun equals(other: Any?) = other is Matrix && other.inner == this.inner

    override fun hashCode() = inner.hashCode()
}
