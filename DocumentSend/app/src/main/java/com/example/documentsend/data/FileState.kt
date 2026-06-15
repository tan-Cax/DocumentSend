package com.example.documentsend.data

import android.net.Uri
import com.example.documentsend.manager.TransferProgress
import com.example.documentsend.network.PacketType

data class FileState (
    val packetType: PacketType? = null, // 传输类型
    val inputIp: String = "",        // IP输入框
    val inputMessage: String = "",   // 消息输入框
    val historyIp: List<String> = emptyList(),  // 历史IP列表
    val selectedUri: Uri? = null,  // 选中的文件URI
    val filename: String = "",  // 选中文件的名称
    val fileSize: Long = 0,  // 选中文件的大小
    val transferProgress: TransferProgress = TransferProgress(),
    val localIpAddress: String = "",  // 本地IP地址
    val userName: String = "默认用户",  // 用户名
    val port: Int = 6666,  // 端口号，端口为计算机上的开放端口，已经配置完毕，硬编码写死
    val autoSave: Boolean = false, // 自动保存
    val pendingSaveHistoryId: Int = -1,     // 待保存的记录ID
    val pendingSaveFileName: String = "",  // 待保存的文件名
    val pendingSaveFileSize: Long = 0,     // 待保存的文件大小
    val pendingSaveTempPath: String = ""   // 待保存的临时路径
)