package com.example.documentsend

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.documentsend.manager.TransferNotificationService
import com.example.documentsend.navigation.AppNavigation
import com.example.documentsend.utils.StorageUtils

class MainActivity : ComponentActivity() {

    // 注册通知权限请求
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                TransferNotificationService.start(this)
            }
        }

    // 注册存储权限系统设置页面回调
    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 从系统设置返回，不做额外处理
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        requestStoragePermission()

        setContent {
            AppNavigation()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            } else {
                TransferNotificationService.start(this)
            }
        } else {
            TransferNotificationService.start(this)
        }
    }

    private fun requestStoragePermission() {
        if (!StorageUtils.hasStoragePermission(this)) {
            AlertDialog.Builder(this)
                .setTitle("需要存储权限")
                .setMessage("为了保存接收到的文件，请授予「所有文件访问」权限。")
                .setPositiveButton("去授权") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    storagePermissionLauncher.launch(intent)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}
