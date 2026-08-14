package com.biwenger_client.core.mvi

import com.biwenger_client.core.coeffects.Coeffect
import com.biwenger_client.core.coeffects.Coeffects
import com.biwenger_client.core.effects.Effect
import com.biwenger_client.core.events.Event
import com.biwenger_client.core.state.Database

class AppStore(
    private val registry: Registry,
    private val database: Database
) : Store {
    override fun dispatch(event: Event<*>) = registry.dispatch(event = event)
    override fun <T> registerEventHandler(name: String, handler: suspend (Event<T>) -> List<Effect>) =
        registry.registerEventHandler(name = name, handler = handler)
    override fun <T> registerEventHandler(
        name: String,
        coeffects: List<Coeffect<*>>,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    ) = registry.registerEventHandler(name = name, coeffects = coeffects, handler = handler)
    override fun <T> removeEventHandler(name: String, handler: suspend (Event<T>) -> List<Effect>) =
        registry.removeEventHandler(name = name, handler = handler)
    override fun <T> removeEventHandler(
        name: String,
        handler: suspend (Event<T>, Coeffects) -> List<Effect>
    ) = registry.removeEventHandler(name = name, handler = handler)
    override fun <T> subscribe(path: String, subscriber: (T) -> Unit) =
        database.subscribe(path = path, subscriber = subscriber)
}
