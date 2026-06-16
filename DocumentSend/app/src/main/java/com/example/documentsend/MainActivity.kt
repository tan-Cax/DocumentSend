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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.documentsend.log.Logger
import com.example.documentsend.manager.TransferNotificationService
import com.example.documentsend.navigation.AppNavigation
import com.example.documentsend.utils.StorageUtils

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                TransferNotificationService.start(this)
            }
        }

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private val backgroundSaveObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) {
            Logger.saveToFile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ProcessLifecycleOwner.get().lifecycle.addObserver(backgroundSaveObserver)

        Logger.i("MainActivity", "App启动")

        requestNotificationPermission()
        requestStoragePermission()

        setContent {
            AppNavigation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(backgroundSaveObserver)
        Logger.i("MainActivity", "App销毁")
        Logger.saveToFile()
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
