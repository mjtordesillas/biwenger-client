package com.biwenger_client.core.state

import kotlinx.coroutines.flow.MutableStateFlow

class Database(
    initialState: Map<String, Any?> = emptyMap()
) {
    private val _state = MutableStateFlow(initialState)
    val subscribers = mutableMapOf<String, MutableList<(Any?) -> Unit>>()

    fun <T> subscribe(path: String, subscriber: (T) -> Unit): () -> Unit {
        if (!subscribers.containsKey(path)) {
            subscribers[path] = mutableListOf()
        }

        val typedSubscriber = subscriber as (Any?) -> Unit
        subscribers[path]?.add(typedSubscriber)
        subscriber(getCurrentStateSlice(path))

        return {
            subscribers[path]?.remove(typedSubscriber)
        }
    }

    fun getCurrentStateSlice(path: String): Any? = _state.value[path]

    fun <T> updateState(path: String, value: T) {
        _state.value += (path to value)
        notifyStateChanges()
    }

    private fun notifyStateChanges() {
        subscribers.forEach { (path, stateSubscribers) ->
            val value = _state.value[path]
            stateSubscribers.forEach { subscriber -> subscriber(value) }
        }
    }
}
