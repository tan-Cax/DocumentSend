package com.example.documentsend.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.documentsend.navigation.Screen
import com.example.documentsend.ui.theme.light_blue
import com.example.documentsend.ui.theme.royal_blue

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable

fun AppBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home),
        BottomNavItem(Screen.Send.route, "Send", Icons.Filled.Send),
        BottomNavItem(Screen.Receive.route, "Receive", Icons.Filled.ArrowDropDown),
        BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings)
    )
    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = royal_blue,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = light_blue
                )
            )
        }
    }
}
