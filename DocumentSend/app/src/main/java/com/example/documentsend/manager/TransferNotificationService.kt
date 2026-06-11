package com.example.documentsend.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.documentsend.utils.StorageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 传输通知服务
 *
 * 前台服务，用于在通知栏显示文件传输进度。
 * 监听 TransferManager 的状态变化，自动更新通知内容。
 *
 * 特点：
 * - 通知不可被用户关闭（setOngoing(true)）
 * - 不响铃、不弹出（IMPORTANCE_LOW + setSilent(true)）
 * - 传输完成自动停止服务并移除通知
 *
 * 使用方式：
 * - 启动：TransferNotificationService.start(context)
 * - 停止：TransferNotificationService.stop(context)
 */
class TransferNotificationService : Service() {

    /** 通知渠道 ID */
    private val CHANNEL_ID = "transfer_progress"

    /** 通知 ID */
    private val NOTIFICATION_ID = 1

    /** 服务协程作用域，用于收集 TransferManager 状态 */
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    companion object {
        //启动前台服务
        fun start(context: Context) {
            val intent = Intent(context, TransferNotificationService::class.java)
            context.startForegroundService(intent)
        }

        //停止前台服务
        fun stop(context: Context) {
            context.stopService(Intent(context, TransferNotificationService::class.java))
        }
    }

    /**
     * 服务创建时调用
     * 1. 创建通知渠道
     * 2. 启动前台服务并显示初始通知
     * 3. 开始监听 TransferManager 状态
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("准备传输...", 0f, false).build())

        // 收集 TransferManager 的状态变化
        serviceScope.launch {
            TransferManager.getInstance().transferState.collect { progress ->
                when (progress.direction) {
                    // 正在传输：更新通知进度
                    TransferDirection.SENDING,
                    TransferDirection.RECEIVING -> updateNotification(progress)
                    // 传输空闲：停止服务，移除通知
                    TransferDirection.IDLE -> stopSelf()
                }
            }
        }
    }

    /**
     * 更新通知内容
     * 根据传输方向（发送/接收）显示不同的标题和图标
     *
     * @param progress 当前传输进度数据
     */
    private fun updateNotification(progress: TransferProgress) {
        val isSend = progress.direction == TransferDirection.SENDING
        val title = if (isSend) {
            "正在发送: ${progress.currentFileName}"
        } else {
            "正在接收: ${progress.currentFileName}"
        }

        val notification = buildNotification(title, progress.progressPercent, isSend).build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 构建通知
     *
     * @param title 通知标题（如 "正在发送: file.txt"）
     * @param progress 传输进度百分比（0.0 ~ 1.0）
     * @param isSend 是否为发送方向（决定图标样式）
     * @return NotificationCompat.Builder 实例
     */
    private fun buildNotification(title: String, progress: Float, isSend: Boolean): NotificationCompat.Builder {
        // 根据传输方向选择图标
        val icon = if (isSend) {
            android.R.drawable.stat_sys_upload
        } else {
            android.R.drawable.stat_sys_download
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOngoing(true)    // 不可被用户关闭
            .setSilent(true)     // 不响铃、不振动
    }

    /**
     * 创建通知渠道（Android 8.0+ 必须）
     * 使用 IMPORTANCE_LOW 确保不响铃、不弹出
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "传输进度",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示文件传输进度"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * 服务销毁时调用
     * 取消协程、移除通知
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        // 移除通知栏中的传输进度通知
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    /**
     * 绑定服务（本服务不支持绑定，返回 null）
     */
    override fun onBind(intent: Intent): IBinder? = null
}
