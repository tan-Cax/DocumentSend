package com.example.documentsend.ui.view

import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import com.example.documentsend.network.PacketType
import com.example.documentsend.ui.components.MainScaffold
import com.example.documentsend.viewmodel.DocViewModel

@Composable
fun Text(navController: NavController,
         viewModel: DocViewModel) {
    val state = viewModel.fileState
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.updateTransferType(PacketType.TEXT)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            if (message == "发送成功") {
                viewModel.updateInputMessage("")
                navController.popBackStack()
            } else if (message.startsWith("发送失败")) {
                errorMessage = message
                showDialog = true
            }
        }
    }

    MainScaffold(
        currentRoute = "text",
        pageTitle = "文本编辑",
        onNavigate = { route -> navController.navigate(route) },
        onBackClick = { navController.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.inputMessage,
                onValueChange = { viewModel.updateInputMessage(it) },
                label = { Text("请输入文本") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (state.inputIp.isBlank()) {
                        Toast.makeText(context, "目标IP为空", Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        return@Button
                    }
                    if (state.inputMessage.isBlank()) {
                        Toast.makeText(context, "文本内容为空", Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        return@Button
                    }
                    viewModel.sendText()
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
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("发送失败") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}
