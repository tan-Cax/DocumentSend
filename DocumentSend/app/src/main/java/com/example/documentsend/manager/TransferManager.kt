package com.example.documentsend.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// 传输方向的枚举
enum class TransferDirection {
    IDLE,       // 空闲状态
    SENDING,    // 正在发送
    RECEIVING   // 正在接收
}

// 记录传输进度的数据类
data class TransferProgress(
    val direction: TransferDirection = TransferDirection.IDLE,
    val currentFileName: String = "",
    val currentBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val progressPercent: Float = 0f // 0.0f 到 1.0f
)

/**
 * 核心传输监控中心：负责收集 Socket 层的高频进度更新，并以响应式数据流(Flow)的形式暴露给 ViewModel。
 * 这是一个单例类，保证跨页面的传输状态一致性。
 */
class TransferManager private constructor() {

    // 内部可变的 StateFlow，用于底层更新状态
    private val _transferState = MutableStateFlow(TransferProgress())
    
    // 对外暴露的只读 StateFlow，供 ViewModel 收集
    val transferState: StateFlow<TransferProgress> = _transferState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: TransferManager? = null

        // 获取单例的方法
        fun getInstance(): TransferManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TransferManager()
                INSTANCE = instance
                instance
            }
        }
    }

    //控制发送进度
    fun updateSendingProgress(fileName: String, currentBytes: Long, totalBytes: Long) {
        val percent = if (totalBytes > 0) currentBytes.toFloat() / totalBytes.toFloat() else 0f
        _transferState.value = TransferProgress(
            direction = TransferDirection.SENDING,
            currentFileName = fileName,
            currentBytes = currentBytes,
            totalBytes = totalBytes,
            progressPercent = percent
        )
    }

    //控制接收进度
    fun updateReceivingProgress(fileName: String, currentBytes: Long, totalBytes: Long) {
        val percent = if (totalBytes > 0) currentBytes.toFloat() / totalBytes.toFloat() else 0f
        _transferState.value = TransferProgress(
            direction = TransferDirection.RECEIVING,
            currentFileName = fileName,
            currentBytes = currentBytes,
            totalBytes = totalBytes,
            progressPercent = percent
        )
    }

    //重置状态
    fun setIdle() {
        _transferState.value = TransferProgress(direction = TransferDirection.IDLE)
    }
}
