package com.example.documentsend.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.ui.theme.*
import com.example.documentsend.viewmodel.LogEntry
import com.example.documentsend.viewmodel.LogViewModel

//日志界面
@Composable
fun Log(navController: NavController, viewModel: LogViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadLogs()
    }

    MainScaffold(
        currentRoute = "log",
        pageTitle = "日志",
        showBottomBar = false,
        onNavigate = { route -> navController.navigate(route) },
        onBackClick = { navController.popBackStack() }
    ) { paddingValues ->
        val logs = viewModel.logs.reversed() // 倒序显示，最新的在最上方

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(logs) { entry ->
                LogItem(entry)
            }
        }
    }
}

@Composable
fun LogItem(entry: LogEntry) {
    val backgroundColor = when (entry.level) {
        "E" -> red
        "W" -> yellow
        "I" -> green
        "D" -> blue
        else -> gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 左侧圆角正方形方块
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.level,
                color = white,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧三行显示
        Column {
            // 第一行：Tag
            Text(
                text = entry.tag,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = black
            )
            // 第二行：Message
            Text(
                text = entry.message,
                fontSize = 14.sp,
                color = black
            )
            // 第三行：时间
            Text(
                text = entry.timestamp,
                fontSize = 12.sp,
                color = gray
            )
        }
    }
}