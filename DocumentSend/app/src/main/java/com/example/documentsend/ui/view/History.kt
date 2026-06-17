package com.example.documentsend.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.ui.theme.white
import com.example.documentsend.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.documentsend.data.HistoryType
import com.example.documentsend.viewmodel.DocViewModel
import com.example.documentsend.utils.StorageUtils
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// 历史记录页面主函数
@Composable
fun History(
    navController: NavController,
    viewModel: HistoryViewModel,
    DocViewModel: DocViewModel
) {
    val historyList = viewModel.historyList
    val clipboardManager = LocalClipboardManager.current

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        DocViewModel.uiEvent.collect { message ->
            dialogMessage = message
            showDialog = true
        }
    }

    // 页面加载时从数据库读取历史记录
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("提示") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清空") },
            text = { Text("确定要清空所有历史记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 页面主体布局
    MainScaffold(
        currentRoute = "history",
        pageTitle = "历史记录",
        onNavigate = { route -> navController.navigate(route) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空历史")
                }
            }
            // 历史记录列表，可滚动
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 遍历每条历史记录，生成卡片
                items(historyList) { item ->
                    HistoryCard(item, DocViewModel)
                }
            }
        }
    }
}

// 单条历史记录卡片
@Composable
fun HistoryCard(history: com.example.documentsend.data.History, docViewModel: DocViewModel) {
    // 时间格式化
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(history.timestamp))
    // 判断是发送还是接收
    val isSend = history.typeString == HistoryType.SEND
    // 判断是否完成
    val isCompleted = history.offset >= history.totalLength && history.totalLength > 0

    // 卡片容器
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 左侧图标 + 中间文件信息 + 右侧大小和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：类型图标（发送/接收）
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSend) Color(0xFFE3F2FD) else Color(0xFFF1F8E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSend) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isSend) Color(0xFF1976D2) else Color(0xFF388E3C),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 中间：文件名和目标IP
                Column(modifier = Modifier.weight(1f)) {
                    // 文件名（单行显示，超长省略）
                    Text(
                        text = history.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // 显示"发送至: IP"或"来自: IP"
                    Text(
                        text = "${if (isSend) "发送至: " else "来自: "}${history.targetIp.ifEmpty { "未知" }}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // 右侧：文件大小和时间
                Column(horizontalAlignment = Alignment.End) {
                    // 文件大小
                    Text(
                        text = StorageUtils.formatFileSize(history.totalLength),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // 传输时间
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // 发送类添加按键（注释：URI权限问题，待内部存储方案实现后启用）
                // if (isSend) {
                //     Spacer(modifier = Modifier.width(8.dp))
                //     Button(
                //         onClick = {
                //             if (isCompleted) {
                //                 docViewModel.sendFromBreakpoint(history.copy(offset = 0))
                //             } else {
                //                 docViewModel.sendFromBreakpoint(history)
                //             }
                //         },
                //         shape = RoundedCornerShape(8.dp),
                //         contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                //         modifier = Modifier.height(32.dp),
                //         colors = ButtonDefaults.buttonColors(
                //             containerColor = if (isCompleted) Color(0xFF1976D2) else Color(0xFFFFA000)
                //         )
                //     ) {
                //         Text(
                //             text = if (isCompleted) "重新发送" else "继续发送",
                //             fontSize = 12.sp,
                //             color = Color.White
                //         )
                //     }
                // }
            }

            // 下方状态小字
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isCompleted) "已完成" else "未完成",
                color = if (isCompleted) Color(0xFF388E3C) else Color(0xFFD32F2F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
