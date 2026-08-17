package com.biwenger_client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import com.biwenger_client.core.navigation.NavigationProvider
import com.biwenger_client.core.navigation.Routes
import com.biwenger_client.features.market.ui.MarketScreen
import com.biwenger_client.features.squad.ui.SquadScreen
import com.biwenger_client.ui.BiwengerClientNavigationBar
import com.biwenger_client.ui.theme.BiwengerClientTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationProvider: NavigationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiwengerClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController: NavHostController = rememberNavController()

                    DisposableEffect(navController) {
                        navigationProvider.setNavController(navController)
                        onDispose { navigationProvider.setNavController(null) }
                    }

                    val currentRoute by navController.currentBackStackEntryAsState()

                    Scaffold(
                        bottomBar = {
                            BiwengerClientNavigationBar(
                                currentRoute = currentRoute?.destination?.route,
                                onNavigate = { route -> navController.navigate(route) { launchSingleTop = true } }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            NavHost(navController = navController, startDestination = Routes.SQUAD) {
                                composable(Routes.SQUAD) { SquadScreen() }
                                composable(Routes.MARKET) { MarketScreen() }
                            }
                        }
                    }
                }
            }
        }
    }
}
