package com.biwenger_client.core.navigation

import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationProvider : Navigator {
    private val _navController = MutableStateFlow<NavController?>(null)
    val navController: StateFlow<NavController?> = _navController.asStateFlow()

    fun setNavController(controller: NavController?) {
        _navController.value = controller
    }

    override fun navigate(route: String) {
        _navController.value?.navigate(route)
    }

    override fun popBackStack() {
        _navController.value?.popBackStack()
    }
}
