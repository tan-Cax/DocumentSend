package com.example.documentsend.ui.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.ui.theme.white
import com.example.documentsend.viewmodel.DocViewModel

@Composable
fun Home (navController: NavController,
          viewModel: DocViewModel
){
    val state = viewModel.fileState
    val uiEvent = viewModel.uiEvent

    val context = LocalContext.current // 获取上下文用于弹 Toast

    // 监听 ViewModel 里的事件流
    LaunchedEffect(key1 = true) {
        uiEvent.collect { message ->
            // 当收到事件时，弹出一个 Toast
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    MainScaffold(
        currentRoute = "home",
        pageTitle = "主页",
        onNavigate = { route -> navController.navigate(route) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(white)
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "我的IP: ${state.localIpAddress}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = { 
                    viewModel.updateLocalIpAddress()
                },
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新IP",
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "重新获取本地ip",
                fontSize = 16.sp
            )
        }
    }
}