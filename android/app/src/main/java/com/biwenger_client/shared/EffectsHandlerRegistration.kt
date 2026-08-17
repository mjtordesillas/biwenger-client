package com.biwenger_client.shared

import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.effects.DispatchEventHandler
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.core.state.Database
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.core.state.UpdateStateHandler

// Squad has no feature-specific effects yet — only the core ones are
// registered. When a feature adds its own effects, this gains one line
// per feature (see interest-tracker's EffectsHandlerRegistration).
class EffectsHandlerRegistration(
    private val registry: Registry,
    private val database: Database,
) {
    fun register() {
        registry.registerEffectHandler(
            effectClass = DispatchEvent::class,
            handler = DispatchEventHandler(registry = registry)
        )
        registry.registerEffectHandler(
            effectClass = UpdateState::class,
            handler = UpdateStateHandler(database)
        )
    }
}
