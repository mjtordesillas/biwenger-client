package com.biwenger_client.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.biwenger_client.core.navigation.Routes
import com.biwenger_client.ui.theme.ColorBgDeep
import com.biwenger_client.ui.theme.Neutral500

// Two-item bottom nav — this app's first navigation surface, ported
// alongside core/navigation/ (see interest-tracker-android's
// InterestTrackerNavigationBar for the reference shape). Trimmed to
// this app's two current destinations; grows the same way that one did
// if a third shows up.
//
// Same color scheme as the top subtab rows (SquadSubTabRow/
// MarketSubTabRow): ColorBgDeep for the bar itself — its own doc
// comment already calls it out for exactly this ("a shade below
// ColorBg, for a nav bar that should read as a distinct layer from
// page content"), just not wired up here until now — primary for the
// selected item, Neutral500 for unselected, no pill indicator behind
// the icon (indicatorColor = Transparent) so the color contrast alone
// carries selection, same as the top tabs' underline-free color swap.
@Composable
fun BiwengerClientNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(containerColor = ColorBgDeep) {
        NavigationBarItem(
            selected = currentRoute == Routes.SQUAD || currentRoute == null,
            onClick = { onNavigate(Routes.SQUAD) },
            icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = "Squad") },
            label = { Text(text = "Squad") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Neutral500,
                unselectedTextColor = Neutral500,
            )
        )
        NavigationBarItem(
            selected = currentRoute == Routes.MARKET,
            onClick = { onNavigate(Routes.MARKET) },
            icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = "Market") },
            label = { Text(text = "Market") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Neutral500,
                unselectedTextColor = Neutral500,
            )
        )
    }
}
