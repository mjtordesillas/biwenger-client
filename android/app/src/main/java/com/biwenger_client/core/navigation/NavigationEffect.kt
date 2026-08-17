package com.biwenger_client.core.navigation

import com.biwenger_client.core.effects.Effect

// Ported from interest-tracker-android. NavigateAndClearBackStack isn't
// carried over — nothing here needs to clear history yet, add it back
// the slice that does.
sealed class NavigationEffect : Effect {
    data class NavigateToRoute(val route: String) : NavigationEffect()
    data object PopBackStack : NavigationEffect()
}
