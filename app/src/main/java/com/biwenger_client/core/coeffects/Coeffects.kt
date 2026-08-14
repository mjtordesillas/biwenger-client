package com.biwenger_client.core.coeffects

import com.biwenger_client.core.state.Loadable

class Coeffects(private val values: Map<Coeffect<*>, Any?>) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(coeffect: Coeffect<T>): T {
        val stored = values[coeffect]
        return if (stored is Loadable.Success<*>) stored.value as T else stored as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> load(coeffect: Coeffect<T>): Loadable<T> {
        val stored = values[coeffect]
        return if (stored is Loadable<*>) stored as Loadable<T> else Loadable.Success(value = stored as T)
    }
}
