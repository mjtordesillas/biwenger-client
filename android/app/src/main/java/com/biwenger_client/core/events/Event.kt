package com.biwenger_client.core.events

data class Event<T>(
    val name: String,
    val payload: T? = null
)

fun <T> event(name: String, payload: T): Event<T> {
    return Event(name, payload)
}

fun event(name: String): Event<Unit> {
    return Event(name)
}
