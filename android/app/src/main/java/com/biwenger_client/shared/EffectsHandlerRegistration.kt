package com.biwenger_client.shared

import com.biwenger_client.core.effects.DispatchEvent
import com.biwenger_client.core.effects.DispatchEventHandler
import com.biwenger_client.core.mvi.Registry
import com.biwenger_client.core.navigation.NavigationEffect
import com.biwenger_client.core.navigation.NavigationEffectHandler
import com.biwenger_client.core.navigation.Navigator
import com.biwenger_client.core.state.Database
import com.biwenger_client.core.state.UpdateState
import com.biwenger_client.core.state.UpdateStateHandler

class EffectsHandlerRegistration(
    private val registry: Registry,
    private val database: Database,
    private val navigator: Navigator,
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
        registry.registerEffectHandler(
            effectClass = NavigationEffect::class,
            handler = NavigationEffectHandler(navigator = navigator)
        )
    }
}
