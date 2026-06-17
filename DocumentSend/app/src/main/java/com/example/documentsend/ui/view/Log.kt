package com.example.documentsend.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.documentsend.viewmodel.LogEntry
import com.example.documentsend.viewmodel.LogViewModel
import com.example.documentsend.ui.theme.blue
import com.example.documentsend.ui.theme.gray
import com.example.documentsend.ui.theme.green
import com.example.documentsend.ui.theme.red
import com.example.documentsend.ui.theme.white
import com.example.documentsend.ui.theme.yellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Log(navController: NavController, viewModel: LogViewModel = viewModel()) {
    var filterExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadLogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日志", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { filterExpanded = true }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "筛选",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { viewModel.selectAllTags() }) {
                                    Text("全选")
                                }
                                TextButton(onClick = { viewModel.clearAllTags() }) {
                                    Text("清空")
                                }
                            }
                            HorizontalDivider()
                            viewModel.allTags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    leadingIcon = {
                                        Checkbox(
                                            checked = tag in viewModel.selectedTags,
                                            onCheckedChange = { viewModel.toggleTag(tag) }
                                        )
                                    },
                                    onClick = { viewModel.toggleTag(tag) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        val logs = viewModel.filteredLogs.reversed()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
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

        Column {
            Text(
                text = entry.tag,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.timestamp,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
