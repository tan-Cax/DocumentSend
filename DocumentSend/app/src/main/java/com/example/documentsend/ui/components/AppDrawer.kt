package com.example.documentsend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.documentsend.ui.theme.light_blue
import com.example.documentsend.ui.theme.royal_blue

@Composable
fun AppDrawerContent(
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = light_blue
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Menu", 
            modifier = Modifier.padding(16.dp), 
            style = MaterialTheme.typography.titleLarge,
            color = royal_blue
        )
        HorizontalDivider(color = Color.LightGray)
        NavigationDrawerItem(
            icon = { Icon(imageVector = Icons.Default.List, contentDescription = "History") },
            label = { Text("History") },
            selected = false,
            onClick = {
                onNavigate("history")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = {
                onNavigate("settings")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
