package com.biwenger_client.core.navigation

import com.biwenger_client.core.effects.EffectHandler

class NavigationEffectHandler(private val navigator: Navigator) : EffectHandler<NavigationEffect> {
    override suspend fun handle(effect: NavigationEffect) {
        when (effect) {
            is NavigationEffect.NavigateToRoute -> navigator.navigate(route = effect.route)
            is NavigationEffect.PopBackStack -> navigator.popBackStack()
        }
    }
}
