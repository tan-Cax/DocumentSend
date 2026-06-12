package com.example.documentsend.ui.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.documentsend.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.ui.theme.white
import com.example.documentsend.viewmodel.SettingsViewModel

// ==================== 设置条目数据 ====================
data class SettingItem(
    val label: String,
    val key: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val settingsState = viewModel.settingsState
    val context = LocalContext.current

    // 弹窗控制
    var showUserNameDialog by remember { mutableStateOf(false) }
    var dialogInput by remember { mutableStateOf(settingsState.userName) }
    var showSendPortDialog by remember { mutableStateOf(false) }
    var sendPortInput by remember { mutableStateOf(settingsState.sendPort.toString()) }
    var showReceivePortDialog by remember { mutableStateOf(false) }
    var receivePortInput by remember { mutableStateOf(settingsState.receivePort.toString()) }
    var showSavePathDialog by remember { mutableStateOf(false) }
    var savePathInput by remember { mutableStateOf(settingsState.savePath) }

    // 主题模式映射
    val themeModeOptions = listOf("白天", "黑夜", "跟随系统")
    val themeModeMap = mapOf("白天" to 1, "黑夜" to 2, "跟随系统" to 0)
    val reverseThemeModeMap = mapOf(0 to "跟随系统", 1 to "白天", 2 to "黑夜")

    MainScaffold(
        currentRoute = "settings",
        pageTitle = "设置",
        onNavigate = { route -> navController.navigate(route) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(white)
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 用户名修改
                item {
                    SettingRow(label = "用户名称") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    dialogInput = settingsState.userName
                                    showUserNameDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = settingsState.userName,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 夜晚模式
                item {
                    SettingRow(label = "夜晚模式") {
                        SimpleDropdown(
                            options = themeModeOptions,
                            selectedOption = reverseThemeModeMap[settingsState.themeMode] ?: "跟随系统",
                            onOptionSelected = { selected ->
                                val mode = themeModeMap[selected] ?: 0
                                viewModel.updateThemeMode(mode)
                            }
                        )
                    }
                }

                // 自动保存
                item {
                    SettingRow(label = "自动保存") {
                        SimpleSwitch(
                            checked = settingsState.autoSave,
                            onCheckedChange = { viewModel.updateAutoSave(it) }
                        )
                    }
                }

                // 系统颜色
                item {
                    SettingRow(label = "系统颜色") {
                        SimpleDropdown(
                            options = listOf("默认", "红色", "绿色"),
                            selectedOption = settingsState.colorScheme,
                            onOptionSelected = { viewModel.updateColorScheme(it) }
                        )
                    }
                }

                // 保存到历史记录
                item {
                    SettingRow(label = "保存到历史记录") {
                        SimpleSwitch(
                            checked = settingsState.saveToHistory,
                            onCheckedChange = { viewModel.updateSaveToHistory(it) }
                        )
                    }
                }

                // 发送端口
                item {
                    SettingRow(label = "发送端口") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    sendPortInput = settingsState.sendPort.toString()
                                    showSendPortDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = settingsState.sendPort.toString(),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 接收端口
                item {
                    SettingRow(label = "接收端口") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    receivePortInput = settingsState.receivePort.toString()
                                    showReceivePortDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = settingsState.receivePort.toString(),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 存储路径
                item {
                    SettingRow(label = "存储路径") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    savePathInput = settingsState.savePath
                                    showSavePathDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (settingsState.savePath.isEmpty()) "默认" else settingsState.savePath,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 关于我们
                item {
                    SettingRow(label = "关于我们") {
                        SimpleButton(
                            text = "查看",
                            onClick = {
                                Toast.makeText(context, "关于我们", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 底部导航分隔线
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFFE0E0E0)
                    )
                }

                // 关于 | 日志 导航链接
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "关于",
                            color = Color(0xFF2196F3),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.About.route)
                            }
                        )
                        Text(
                            text = "日志",
                            color = Color(0xFF2196F3),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Log.route)
                            }
                        )
                    }
                }
            }
        }
    }

    // 修改用户名弹窗
    if (showUserNameDialog) {
        AlertDialog(
            onDismissRequest = { showUserNameDialog = false },
            title = { Text(text = "设置用户名", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = dialogInput,
                    onValueChange = { dialogInput = it },
                    placeholder = { Text("请输入用户名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = dialogInput.ifBlank { "未设置用户名" }
                        viewModel.updateUserName(name)
                        showUserNameDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUserNameDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 修改发送端口弹窗
    if (showSendPortDialog) {
        AlertDialog(
            onDismissRequest = { showSendPortDialog = false },
            title = { Text(text = "设置发送端口", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = sendPortInput,
                    onValueChange = { sendPortInput = it },
                    placeholder = { Text("请输入端口号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val port = sendPortInput.toIntOrNull()
                        if (port != null && port in 1..65535) {
                            viewModel.updateSendPort(port)
                            showSendPortDialog = false
                        } else {
                            Toast.makeText(context, "端口号无效", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSendPortDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 修改接收端口弹窗
    if (showReceivePortDialog) {
        AlertDialog(
            onDismissRequest = { showReceivePortDialog = false },
            title = { Text(text = "设置接收端口", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = receivePortInput,
                    onValueChange = { receivePortInput = it },
                    placeholder = { Text("请输入端口号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val port = receivePortInput.toIntOrNull()
                        if (port != null && port in 1..65535) {
                            viewModel.updateReceivePort(port)
                            showReceivePortDialog = false
                        } else {
                            Toast.makeText(context, "端口号无效", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReceivePortDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 修改存储路径弹窗
    if (showSavePathDialog) {
        AlertDialog(
            onDismissRequest = { showSavePathDialog = false },
            title = { Text(text = "设置存储路径", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = savePathInput,
                        onValueChange = { savePathInput = it },
                        placeholder = { Text("留空使用默认路径") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "默认路径: /storage/emulated/0/Download/DocumentSend",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateSavePath(savePathInput.trim())
                        showSavePathDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSavePathDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

// 设置行
@Composable
fun SettingRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black
        )
        content()
    }
}

// 下拉框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Box(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedOption,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 开关
@Composable
fun SimpleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF4CAF50),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFBDBDBD)
        )
    )
}

// 按键
@Composable
fun SimpleButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F0F0))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}
