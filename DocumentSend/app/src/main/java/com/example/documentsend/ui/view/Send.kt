package com.example.documentsend.ui.view

import android.app.AlertDialog
import android.net.Uri
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentsend.R
import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.manager.DiscoveredDevice
import com.example.documentsend.network.PacketType
import androidx.compose.material3.MaterialTheme
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.utils.HistoryUtils
import com.example.documentsend.viewmodel.DocViewModel
import com.example.documentsend.ui.theme.LightError
import com.example.documentsend.ui.theme.LightSuccess

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Send(navController: NavController,
         viewModel: DocViewModel) {
    val state = viewModel.fileState
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateSelectedUri(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            dialogMessage = message
            showDialog = true
        }
    }

    MainScaffold(
        currentRoute = "send",
        pageTitle = "发送",
        onNavigate = { route -> navController.navigate(route) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.updateTransferType(PacketType.TEXT)
                        navController.navigate("text") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "文本")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("文本", softWrap = false)
                    }
                }

                Button(
                    onClick = {
                        filePicker.launch("image/*")
                        viewModel.updateTransferType(PacketType.IMAGE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "图片")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("图片", softWrap = false)
                    }
                }

                Button(
                    onClick = {
                        val clipboard = clipboardManager.getText()?.text
                        if (!clipboard.isNullOrEmpty()) {
                            viewModel.updateInputMessage(clipboard)
                            Toast.makeText(context, "粘贴成功", Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                            viewModel.updateTransferType(PacketType.TEXT)
                        }
                        else {
                            AlertDialog.Builder(context)
                                .setTitle("提示")
                                .setMessage("剪贴板没有内容")
                                .setNeutralButton("关闭"){ dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "剪贴板")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("剪贴板", softWrap = false)
                    }
                }

                Button(
                    onClick = {
                        filePicker.launch("*/*")
                        viewModel.updateTransferType(PacketType.FILE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.List, contentDescription = "文件")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("文件", softWrap = false)
                    }
                }

                Button(
                    onClick = {
                        filePicker.launch("video/*")
                        viewModel.updateTransferType(PacketType.VIDEO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "视频")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("视频", softWrap = false)
                    }
                }

                Button(
                    onClick = {
                        filePicker.launch("application/zip")
                        viewModel.updateTransferType(PacketType.ARCHIVE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "压缩包")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("压缩包", softWrap = false)
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "寻找设备",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.refreshUdp() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.discoveredDevices.isEmpty()) {
                Text(
                    text = "暂无设备",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                var selectedUuid by remember { mutableStateOf<String?>(null) }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.discoveredDevices) { device ->
                        val isSelected = device.uuid == selectedUuid
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedUuid = device.uuid
                                    viewModel.updateInputIp(device.ip)
                                    viewModel.updatePort(device.tcpPort)
                                }
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (device.device == "pc") Icons.Default.Computer else Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = device.deviceName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = device.ip,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.inputIp,
                onValueChange = { viewModel.updateInputIp(it) },
                label = { Text("目标 IP") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if(state.inputIp.isBlank()) {
                        Toast.makeText(context, "目标IP为空", Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        return@Button
                    }
                    if(state.packetType == null) {
                        Toast.makeText(context, "当前无发送内容", Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        return@Button
                    }

                    when (state.packetType) {
                        PacketType.TEXT -> viewModel.sendText()
                        else -> viewModel.sendFile()
                    }
                    viewModel.updateTransferType(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("发送")
            }
            Text(
                text = "当前发送类型: ${state.packetType}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            var showCopyDialog by remember { mutableStateOf(false) }
            var selectedRecord by remember { mutableStateOf<History?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.sendSessionRecords) { record ->
                    val isSend = record.typeString == HistoryType.SEND
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isText) "[文本]" else "[文件]",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isText) record.text else record.name,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCompleted) "✓ 成功" else "✗ 发送失败",
                            fontSize = 12.sp,
                            color = if (isCompleted) LightSuccess else LightError
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
    }
}
