package com.biwenger_client.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.biwenger_client.core.navigation.Routes
import com.biwenger_client.ui.theme.ColorAccent

// Two-item bottom nav — this app's first navigation surface, ported
// alongside core/navigation/ (see interest-tracker-android's
// InterestTrackerNavigationBar for the reference shape). Trimmed to
// this app's two current destinations; grows the same way that one did
// if a third shows up.
@Composable
fun BiwengerClientNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.SQUAD || currentRoute == null,
            onClick = { onNavigate(Routes.SQUAD) },
            icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = "Squad") },
            label = { Text(text = "Squad") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = ColorAccent,
                selectedTextColor = ColorAccent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.MARKET,
            onClick = { onNavigate(Routes.MARKET) },
            icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = "Market") },
            label = { Text(text = "Market") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = ColorAccent,
                selectedTextColor = ColorAccent
            )
        )
    }
}
