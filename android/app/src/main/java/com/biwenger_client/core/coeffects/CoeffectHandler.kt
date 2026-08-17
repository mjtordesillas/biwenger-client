package com.biwenger_client.core.coeffects

import com.biwenger_client.core.state.Loadable

interface CoeffectHandler<in C : Coeffect<T>, out T> {
    suspend fun extract(coeffect: C): T

    suspend fun onFailure(coeffect: C, error: Throwable): Loadable.Failed =
        Loadable.Failed(error = error)
}
