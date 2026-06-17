package com.example.documentsend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 通用的页面脚手架组件
 * 将顶栏、底栏、侧边栏统合。其他页面直接嵌套即可使用统一的UI框架。
 * onBackClick 为 null 时显示菜单图标，非 null 时显示返回图标。
 */
@Composable
fun MainScaffold(
    currentRoute: String,
    pageTitle: String,
    showBottomBar: Boolean = true,
    onNavigate: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawerContent(onNavigate = onNavigate, currentRoute = currentRoute) }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = pageTitle,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(all = 16.dp),
            ) {
                content(paddingValues)
            }
        }
    }
}
