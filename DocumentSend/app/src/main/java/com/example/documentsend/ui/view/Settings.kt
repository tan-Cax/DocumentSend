package com.example.documentsend.ui.view

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.documentsend.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.viewmodel.SettingsViewModel
import com.example.documentsend.ui.theme.DarkSwitchOff
import com.example.documentsend.ui.theme.DarkSwitchOn
import com.example.documentsend.ui.theme.LightSwitchOff
import com.example.documentsend.ui.theme.LightSwitchOn


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val settingsState = viewModel.settingsState
    val context = LocalContext.current

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // 获取并清理路径
            val documentId = android.provider.DocumentsContract.getTreeDocumentId(it)
            val path = if (documentId.startsWith("primary:")) {
                "${android.os.Environment.getExternalStorageDirectory()}/${documentId.removePrefix("primary:")}"
            } else {
                // 如果是其他存储卷，暂时只显示 documentId 的部分，或者直接存 URI (取决于应用后续如何使用)
                // 这里为了保持 java.io.File 兼容性，尝试拼接
                "/storage/${documentId.replace(":", "/")}"
            }
            viewModel.updateSavePath(path)
        }
    }

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
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 用户名修改
                item {
                    SettingRow(label = "用户名称") {
                        SettingInput(
                            value = settingsState.userName,
                            onValueChange = { viewModel.updateUserName(it) },
                            placeholder = "请输入用户名"
                        )
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
                        SettingInput(
                            value = settingsState.sendPort.toString(),
                            onValueChange = {
                                val port = it.toIntOrNull()
                                if (port != null && port in 1..65535) {
                                    viewModel.updateSendPort(port)
                                } else if (it.isEmpty()) {
                                    viewModel.updateSendPort(0)
                                }
                            },
                            placeholder = "端口号",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // 接收端口
                item {
                    SettingRow(label = "接收端口") {
                        SettingInput(
                            value = settingsState.receivePort.toString(),
                            onValueChange = {
                                val port = it.toIntOrNull()
                                if (port != null && port in 1..65535) {
                                    viewModel.updateReceivePort(port)
                                } else if (it.isEmpty()) {
                                    viewModel.updateReceivePort(0)
                                }
                            },
                            placeholder = "端口号",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // 存储路径
                item {
                    SettingRow(label = "存储路径") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(1f)
                        ) {
                    Text(
                        text = if (settingsState.savePath.isEmpty()) "默认" else settingsState.savePath,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = 8.dp),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            SimpleButton(
                                text = "选择",
                                onClick = {
                                    directoryPickerLauncher.launch(null)
                                }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // 底部导航分隔线
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
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
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.About.route)
                            }
                        )
                        Text(
                            text = "日志",
                            color = MaterialTheme.colorScheme.primary,
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
            color = MaterialTheme.colorScheme.onSurface
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedOption,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val isDark = isSystemInDarkTheme()
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.surface,
            checkedTrackColor = if (isDark) DarkSwitchOn else LightSwitchOn,
            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = if (isDark) DarkSwitchOff else LightSwitchOff
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// 设置输入框
@Composable
fun SettingInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    // 过滤掉为 "0" 的端口显示内容，让输入框可以清空重新输入
    val displayValue = if (value == "0") "" else value

    // 使用 TextFieldValue 来手动管理光标位置
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(displayValue, androidx.compose.ui.text.TextRange(displayValue.length)))
    }

    // 当外部值变化且与本地不一致时，同步值并保持光标在末尾
    if (textFieldValue.text != displayValue) {
        textFieldValue = textFieldValue.copy(
            text = displayValue,
            selection = androidx.compose.ui.text.TextRange(displayValue.length)
        )
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            if (it.text != displayValue) {
                onValueChange(it.text)
            }
        },
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        ),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterEnd) {
                if (displayValue.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
                innerTextField()
            }
        }
    )
}
