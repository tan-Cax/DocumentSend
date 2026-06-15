package com.example.documentsend.ui.view

import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentsend.R
import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.manager.TransferDirection
import com.example.documentsend.network.PacketType
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.ui.theme.dark_blue
import com.example.documentsend.ui.theme.white
import com.example.documentsend.utils.HistoryUtils
import com.example.documentsend.viewmodel.DocViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Receive(
    navController: NavController,
    viewModel: DocViewModel
) {
    val state = viewModel.fileState
    val context = LocalContext.current

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val documentId = android.provider.DocumentsContract.getTreeDocumentId(it)
            val path = if (documentId.startsWith("primary:")) {
                "${android.os.Environment.getExternalStorageDirectory()}/${documentId.removePrefix("primary:")}"
            } else {
                "/storage/${documentId.replace(":", "/")}"
            }
            viewModel.savePendingFile(path)
        }
    }

    MainScaffold(
        currentRoute = "receive",
        pageTitle = "接收",
        onNavigate = { route -> navController.navigate(route) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(white)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 用户名
            Text(
                text = state.userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 用户IP
            Text(
                text = state.localIpAddress,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 传输进度显示
            if (state.transferProgress.direction == TransferDirection.RECEIVING) {
                val percent = (state.transferProgress.progressPercent * 100).toInt()
                Text(
                    text = "正在接收: ${state.transferProgress.currentFileName}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$percent%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            } else if (state.transferProgress.direction == TransferDirection.IDLE && state.transferProgress.progressPercent > 0) {
                Text(
                    text = "最近接收完成",
                    fontSize = 14.sp,
                    color = Color.Green
                )
            }

            // 接收记录列表
            Spacer(modifier = Modifier.height(16.dp))

            var showCopyDialog by remember { mutableStateOf(false) }
            var selectedRecord by remember { mutableStateOf<History?>(null) }
            val clipboardManager = LocalClipboardManager.current

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.LightGray)
                    .background(Color(0xFFFAFAFA))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.receiveSessionRecords) { record ->
                    val isText = record.filetypeString == PacketType.TEXT.name
                    val isCompleted = record.offset >= record.totalLength && record.totalLength > 0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    selectedRecord = record
                                    showCopyDialog = true
                                }
                            )
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = HistoryUtils.formatTimestamp(record.timestamp),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = if (isText) "[文本]" else "[文件]",
                                fontSize = 12.sp,
                                color = dark_blue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isText) record.text else record.name,
                            fontSize = 14.sp,
                            color = Color.Black,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCompleted) "✓ 成功" else "✗ 接收失败",
                            fontSize = 12.sp,
                            color = if (isCompleted) Color(0xFF388E3C) else Color(0xFFD32F2F)
                        )
                    }
                }
            }

            if (showCopyDialog && selectedRecord != null) {
                AlertDialog(
                    onDismissRequest = { showCopyDialog = false },
                    title = { Text("复制") },
                    text = { Text("是否复制该记录的内容？") },
                    confirmButton = {
                        TextButton(onClick = {
                            val record = selectedRecord
                            if (record != null) {
                                val textToCopy = if (record.filetypeString == PacketType.TEXT.name) {
                                    record.text
                                } else {
                                    record.name
                                }
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                            }
                            showCopyDialog = false
                        }) {
                            Text("复制")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCopyDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 自动保存状态
            Text(
                text = "自动保存：${if (state.autoSave) "开" else "关"}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 手动保存逻辑
            if (!state.autoSave && state.pendingSaveFileName.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "接收到了文件：${state.pendingSaveFileName}",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelPendingSave() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = { directoryPickerLauncher.launch(null) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}
