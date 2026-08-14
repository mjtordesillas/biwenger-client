package com.biwenger_client.core.mvi

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event

interface Store {
    fun dispatch(event: Event<*>)
    fun <T> registerEventHandler(name: String, handler: suspend (Event<T>) -> List<Effect>)
    fun <T> registerEventHandler(
        name: String,
        coeffects: List<Coeffect<*>>,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    )
    fun <T> registerEventHandler(
        name: String,
        coeffects: (Event<T>) -> List<Coeffect<*>>,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    )
    fun <T> removeEventHandler(name: String, handler: suspend (Event<T>) -> List<Effect>)
    fun <T> removeEventHandler(
        name: String,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    )
    fun <T> subscribe(path: String, subscriber: (T) -> Unit): () -> Unit
}
