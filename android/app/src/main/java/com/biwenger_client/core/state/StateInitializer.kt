package com.biwenger_client.core.state

interface StateInitializer {
    fun initialState(): Map<String, Any?>
}
