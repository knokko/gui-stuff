package procmodel.lang.types

import procmodel.exceptions.PmRuntimeError
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class PmValue {

    @Throws(PmRuntimeError::class)
    inline fun <reified T> castTo(): T {
        if (this is T) {
            return this
        } else {
            throw PmRuntimeError("Expected ${T::class.simpleName}, but found $this")
        }
    }

    @Throws(PmRuntimeError::class)
    fun <T : Any> castTo(expected: KClass<T>): T {
        if (expected.isInstance(this)) {
            return expected.cast(this)
        } else {
            throw PmRuntimeError("Expected ${expected.simpleName}, but found $this")
        }
    }

    abstract fun copy(): PmValue

    @Throws(PmRuntimeError::class)
    open fun setProperty(propertyName: String, newValue: PmValue) {
        throw PmRuntimeError("$this doesn't have properties")
    }

    @Throws(PmRuntimeError::class)
    open fun getProperty(propertyName: String): PmValue {
        throw PmRuntimeError("$this doesn't have properties")
    }

    @Throws(PmRuntimeError::class)
    open fun floatValue(): Float {
        throw PmRuntimeError("$this is not of type float")
    }

    @Throws(PmRuntimeError::class)
    open fun intValue(): Int {
        throw PmRuntimeError("$this is not of type int")
    }

    @Throws(PmRuntimeError::class)
    open fun booleanValue(): Boolean {
        throw PmRuntimeError("$this is not of type boolean")
    }

    @Throws(PmRuntimeError::class)
    open operator fun div(right: PmValue): PmValue {
        throw PmRuntimeError("No semantics for ${this::class.simpleName} / ${right::class.simpleName}")
    }

    @Throws(PmRuntimeError::class)
    open operator fun times(right: PmValue): PmValue {
        throw PmRuntimeError("No semantics for ${this::class.simpleName} * ${right::class.simpleName}")
    }

    @Throws(PmRuntimeError::class)
    open operator fun minus(right: PmValue): PmValue {
        throw PmRuntimeError("No semantics for ${this::class.simpleName} - ${right::class.simpleName}")
    }

    @Throws(PmRuntimeError::class)
    open operator fun plus(right: PmValue): PmValue {
        throw PmRuntimeError("No semantics for ${this::class.simpleName} + ${right::class.simpleName}")
    }

    @Throws(PmRuntimeError::class)
    open operator fun compareTo(right: PmValue): Int {
        throw PmRuntimeError("No semantics for ${this::class.simpleName} cmp ${right::class.simpleName}")
    }

    @Throws(PmRuntimeError::class)
    open operator fun get(key: PmValue): PmValue {
        throw PmRuntimeError("No semantics for ${this::class.simpleName}[$key]")
    }

    @Throws(PmRuntimeError::class)
    open operator fun set(key: PmValue, value: PmValue) {
        throw PmRuntimeError("No semantics for ${this::class.simpleName}[$key] = $value")
    }
}
