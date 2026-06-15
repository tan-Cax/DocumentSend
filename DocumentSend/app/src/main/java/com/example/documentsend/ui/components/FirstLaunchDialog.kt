package com.example.documentsend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.documentsend.R
import kotlinx.coroutines.delay

@Composable
fun FirstLaunchDialog(
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableStateOf(10) }
    var canClose by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        canClose = true
    }

    AlertDialog(
        onDismissRequest = {
            if (canClose) {
                onDismiss()
            }
        },
        title = {
            Text(text = "使用须知")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit height if notes are long
            ) {
                Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    val notesText = LocalContext.current.resources.getStringArray(R.array.notes).joinToString("\n")
                    Text(text = notesText)
                }

                if (!canClose) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请阅读并在 ${countdown}s 后关闭",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            if (canClose) {
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        }
    )
}
