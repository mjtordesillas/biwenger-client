package com.biwenger_client.core.navigation

interface Navigator {
    fun navigate(route: String)
    fun popBackStack()
}
