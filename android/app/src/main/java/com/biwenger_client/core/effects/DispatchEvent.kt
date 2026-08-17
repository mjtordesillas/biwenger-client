package com.biwenger_client.core.effects

import com.biwenger_client.core.events.Event
import com.biwenger_client.core.mvi.Registry

data class DispatchEvent(val event: Event<*>) : Effect

class DispatchEventHandler(
    private val registry: Registry,
) : EffectHandler<DispatchEvent> {
    override suspend fun handle(effect: DispatchEvent) {
        registry.dispatch(effect.event)
    }
}
