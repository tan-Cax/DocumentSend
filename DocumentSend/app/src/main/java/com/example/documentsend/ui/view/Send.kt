package com.example.documentsend.ui.view

import android.app.AlertDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.documentsend.network.PacketType
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.ui.theme.blue
import com.example.documentsend.ui.theme.sky_blue
import com.example.documentsend.ui.theme.white
import com.example.documentsend.viewmodel.DocViewModel

@Composable
fun Send(navController: NavController,
         viewModel: DocViewModel) {
    val state = viewModel.fileState
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateSelectedUri(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    MainScaffold(
        currentRoute = "send",
        pageTitle = "发送",
        onNavigate = { route -> navController.navigate(route) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(white)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // 超出屏幕宽度时允许左右滚动
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.updateTransferType(PacketType.TEXT)
                        navController.navigate("text") },
                    colors = ButtonDefaults.buttonColors(containerColor = sky_blue),
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
                        // 文字不换行
                        Text("文本", softWrap = false)
                    }
                }
                
                Button(
                    onClick = {
                        // 打开系统文件选择器，限制只能选择图片类型
                        filePicker.launch("image/*")
                        viewModel.updateTransferType(PacketType.IMAGE)
                              },
                    colors = ButtonDefaults.buttonColors(containerColor = sky_blue),
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
                            Toast.makeText(context, "粘贴成功", Toast.LENGTH_SHORT).show()
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
                    colors = ButtonDefaults.buttonColors(containerColor = sky_blue),
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
                        // 允许选择任意类型的文件
                        filePicker.launch("*/*")
                        viewModel.updateTransferType(PacketType.FILE)
                        },
                    colors = ButtonDefaults.buttonColors(containerColor = sky_blue),
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
                        // 允许选择视频类型的文件
                        filePicker.launch("video/*")
                        viewModel.updateTransferType(PacketType.VIDEO)
                         },
                    colors = ButtonDefaults.buttonColors(containerColor = sky_blue),
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
                        // 允许选择zip类型的文件
                        filePicker.launch("application/zip")
                        viewModel.updateTransferType(PacketType.ARCHIVE)
                        },
                    colors = ButtonDefaults.buttonColors(containerColor = sky_blue),
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

            Spacer(modifier = Modifier.height(32.dp)) // 拉开与下方的距离

            OutlinedTextField(
                value = state.inputIp,
                onValueChange = { viewModel.updateInputIp(it) },
                label = { Text("目标 IP") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if(state.inputIp.isBlank()) {
                        Toast.makeText(context, "目标IP为空", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // 如果当前没有发送内容则提示选择发送内容
                    if(state.packetType == null) {
                        Toast.makeText(context, "当前无发送内容", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 传输
                    // 功能未完善，此处待补全
                    viewModel.sendText()
                    viewModel.updateTransferType(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                Text("发送")
            }
            Text(
                text = "当前发送类型: ${state.packetType}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}