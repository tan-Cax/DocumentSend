package com.example.documentsend.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentsend.ui.components.MainScaffold

@Composable
fun About(navController: NavController) {
    MainScaffold(
        currentRoute = "about",
        pageTitle = "关于",
        showBottomBar = false,
        onNavigate = { navController.navigate(it) },
        onBackClick = { navController.popBackStack() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DocumentSend", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "v1.0", fontSize = 14.sp)
            }
        }
    }
}
