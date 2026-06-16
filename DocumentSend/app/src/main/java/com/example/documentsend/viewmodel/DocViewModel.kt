package com.example.documentsend.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.documentsend.data.AppDatabase
import com.example.documentsend.data.FileState
import com.example.documentsend.data.History
import com.example.documentsend.log.Logger
import com.example.documentsend.manager.ReceiveManager
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.manager.UdpManager
import com.example.documentsend.network.SocketClient
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.documentsend.network.PacketType
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.network.handlers.send.SendContent
import com.example.documentsend.utils.DeviceUuidUtils
import com.example.documentsend.repository.SettingsRepository
import com.example.documentsend.repository.dataStore
import com.example.documentsend.utils.FileUtils
import com.example.documentsend.utils.GetLocalIP
import com.example.documentsend.utils.HistoryUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.channels.BufferOverflow
import java.io.File

class DocViewModel(application: Application) :
    AndroidViewModel(application) {

    private val transferManager = TransferManager.getInstance()
    private val udpManager = UdpManager.getInstance()
    private val settingsRepository = SettingsRepository(application.dataStore)
    private val historyDao = AppDatabase.getDatabase(application).historyDao()
    private val historyRepository = HistoryRepository(historyDao)
    private val ipDao = AppDatabase.getDatabase(application).ipAddressDao()
    private val repository = Repository(ipDao)
    private val _uiEvent = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var saveToHistory = true

    private val socketSendClient = SocketClient(
        context = application,
        transferManager = transferManager,
        historyRepository = historyRepository
    )

    private val receiveManager = ReceiveManager.getInstance()

    val uiEvent = _uiEvent.asSharedFlow()
    val sendSessionRecords = mutableStateListOf<History>()
    val receiveSessionRecords = mutableStateListOf<History>()
    var fileState by mutableStateOf(FileState())
        private set

    init {
        receiveManager.init(
            context = application,
            transferManager = transferManager,
            historyRepository = historyRepository
        )

        viewModelScope.launch {
            val localIp = GetLocalIP.getLocalIpAddress()
            fileState = fileState.copy(localIpAddress = localIp ?: "未获取到IP地址")
        }

        // 创建uuid并启动UDP广播
        viewModelScope.launch {
            val uuid = DeviceUuidUtils.getOrCreate(application)
            val receivePort = settingsRepository.receivePortFlow.first()
            val userName = settingsRepository.userNameFlow.first()
            fileState = fileState.copy(deviceUuid = uuid)
            udpManager.start(uuid, receivePort, userName)
        }

        // 收集局域网发现的设备
        viewModelScope.launch {
            udpManager.discoveredDevices.collect { devices ->
                fileState = fileState.copy(discoveredDevices = devices)
            }
        }

        viewModelScope.launch {
            settingsRepository.userNameFlow.collect { userName ->
                fileState = fileState.copy(userName = userName)
            }
        }
        viewModelScope.launch {
            val historyIp = repository.getIpHistory()
            val ipList = historyIp.first().map { it.ip }
            fileState = fileState.copy(historyIp = ipList)
        }
        viewModelScope.launch {
            transferManager.transferState.collect { progress ->
                fileState = fileState.copy(transferProgress = progress)
            }
        }
        viewModelScope.launch {
            settingsRepository.saveToHistoryFlow.collect { saveToHistory = it }
        }
        viewModelScope.launch {
            settingsRepository.autoSaveFlow.collect { autoSave ->
                fileState = fileState.copy(autoSave = autoSave)
                // 关键修正：设置改变时动态重新初始化接收端并重启
                receiveManager.init(
                    context = application,
                    transferManager = transferManager,
                    historyRepository = historyRepository,
                    autoSave = autoSave
                )
                receiveManager.restartServer()
            }
        }
        viewModelScope.launch {
            settingsRepository.sendPortFlow.collect { port ->
                fileState = fileState.copy(port = port)
            }
        }
        viewModelScope.launch {
            settingsRepository.targetIpFlow.collect { ip ->
                fileState = fileState.copy(inputIp = ip)
            }
        }

        // 启动接收服务器
        viewModelScope.launch {
            val receivePort = settingsRepository.receivePortFlow.first()
            startServer(receivePort)
        }
    }

    fun startServer(port: Int) {
        receiveManager.startServer(port, object : INetworkListener {
            override fun onConnected(clientIp: String) {
            }
            override fun onDisconnected() {
            }
            override fun onTextMessage(text: String) {
                viewModelScope.launch { _uiEvent.emit("收到文本: $text") }
            }
            override fun onFileStarted(fileName: String, totalLength: Long) {
            }
            override fun onFileProgress(fileName: String, currentLength: Long, totalLength: Long) {
            }
            override fun onFileFinished(fileName: String) {
            }
            override fun onFileReadyToSave(historyId: Int, fileName: String, fileSize: Long, tempPath: String) {
                fileState = fileState.copy(
                    pendingSaveHistoryId = historyId,
                    pendingSaveFileName = fileName,
                    pendingSaveFileSize = fileSize,
                    pendingSaveTempPath = tempPath
                )
                viewModelScope.launch { _uiEvent.emit("收到文件: $fileName") }
            }
            override fun onError(message: String) {
                viewModelScope.launch { _uiEvent.emit("错误: $message") }
            }
            override fun onReceiveRecord(record: History) {
                receiveSessionRecords.add(0, record)
            }
        })
    }

    fun stopServer() {
        receiveManager.stopServer()
    }

    fun refreshUdp() {
        udpManager.refreshDevices()
    }

    fun sendText() {
        val type = fileState.packetType ?: return
        viewModelScope.launch {
            val history = HistoryUtils.createTextSendRecoder(
                content = fileState.inputMessage,
                targetIp = fileState.inputIp,
            )
            val historyId = historyRepository.insertHistory(history)

            val content = SendContent.fromText(fileState.inputMessage)
            val result = socketSendClient.send(fileState.inputIp, fileState.port, type, content)
            result.onSuccess {
                historyRepository.updateOffset(historyId.toInt(), history.totalLength)
                sendSessionRecords.add(0, history.copy(id = historyId.toInt(), offset = history.totalLength))
                Logger.logInfo("ViewModel", "TextSendSuccess", "文本发送成功")
                _uiEvent.emit("发送成功")
            }.onFailure { e ->
                sendSessionRecords.add(0, history.copy(id = historyId.toInt()))
                Logger.logError("ViewModel", "TextSendFailed", e)
                _uiEvent.emit("发送失败: ${e.message}")
            }
        }
    }

    fun sendFile() {
        val type = fileState.packetType ?: return
        val uri = fileState.selectedUri ?: return

        viewModelScope.launch {
            val history = HistoryUtils.createSendRecord(
                fileName = fileState.filename,
                uri = uri,
                fileType = type,
                targetIp = fileState.inputIp,
                totalLength = fileState.fileSize
            )
            val historyId = historyRepository.insertHistory(history)

            val content = SendContent.fromUri(
                getApplication<Application>(),
                uri,
                offset = 0,
                historyId = historyId.toInt()
            )
            if (content == null) {
                sendSessionRecords.add(0, history.copy(id = historyId.toInt()))
                _uiEvent.emit("无法获取文件内容")
                return@launch
            }

            val result = socketSendClient.send(fileState.inputIp, fileState.port, type, content)
            result.onSuccess {
                sendSessionRecords.add(0, history.copy(id = historyId.toInt(), offset = history.totalLength))
                Logger.logInfo("ViewModel", "FileSendSuccess", "文件发送成功: ${fileState.filename}")
            }.onFailure { e ->
                sendSessionRecords.add(0, history.copy(id = historyId.toInt()))
                Logger.logError("ViewModel", "FileSendFailed", e)
                _uiEvent.emit("发送失败: ${e.message}")
            }
        }
    }

    fun sendFromBreakpoint(history: History) {
        val uri = HistoryUtils.getUri(history) ?: return
        val type = HistoryUtils.getPacketType(history) ?: return

        viewModelScope.launch {
            try {
                val content = SendContent.fromUri(
                    getApplication<Application>(),
                    uri,
                    offset = history.offset,
                    historyId = history.id
                )
                if (content == null) {
                    _uiEvent.emit("无法获取文件内容")
                    return@launch
                }

                val result = socketSendClient.send(history.targetIp, fileState.port, type, content)
                result.onSuccess {
                    sendSessionRecords.add(0, history.copy(offset = history.totalLength))
                    Logger.logInfo("ViewModel", "BreakpointSendSuccess", "续传成功: ${history.name}")
                    _uiEvent.emit("续传成功")
                }.onFailure { e ->
                    sendSessionRecords.add(0, history)
                    Logger.logError("ViewModel", "BreakpointSendFailed", e)
                    _uiEvent.emit("续传失败: ${e.message}")
                }
            } catch (e: Exception) {
                _uiEvent.emit("发送失败: ${e.message}")
            }
        }
    }

    fun updateInputIp(ip: String) {
        fileState = fileState.copy(inputIp = ip)
        viewModelScope.launch {
            settingsRepository.setTargetIp(ip)
        }
    }

    fun updateInputMessage(message: String) {
        fileState = fileState.copy(inputMessage = message)
    }

    fun updateTransferType(packetType: PacketType?) {
        fileState = fileState.copy(packetType = packetType)
    }

    fun updateSelectedUri(uri: Uri?) {
        if (uri != null) {
            val context = getApplication<Application>()
            val fileName = FileUtils.getFileName(context, uri)
            val fileSize = FileUtils.getFileSize(context, uri)
            fileState = fileState.copy(
                selectedUri = uri,
                filename = fileName,
                fileSize = fileSize
            )
        } else {
            fileState = fileState.copy(
                selectedUri = null,
                filename = "",
                fileSize = 0
            )
        }
    }

    fun updateFilename(filename: String) {
        fileState = fileState.copy(filename = filename)
    }

    fun updateUserName(userName: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(userName)
        }
    }

    fun updateLocalIpAddress() {
        viewModelScope.launch {
            val localIp = GetLocalIP.getLocalIpAddress()
            if (localIp != null) {
                fileState = fileState.copy(localIpAddress = localIp)
            } else {
                _uiEvent.emit("未获取到IP地址")
            }
        }
    }

    fun savePendingFile(targetPath: String) {
        val tempPath = fileState.pendingSaveTempPath
        val fileName = fileState.pendingSaveFileName
        if (tempPath.isEmpty() || fileName.isEmpty()) return

        viewModelScope.launch {
            try {
                val tempFile = File(tempPath)
                if (!tempFile.exists()) {
                    _uiEvent.emit("临时文件不存在")
                    return@launch
                }

                val targetDir = File(targetPath)
                if (!targetDir.exists()) targetDir.mkdirs()

                //重名文件检测
                var targetFile = File(targetDir, fileName)
                var suffix = 1
                while (targetFile.exists()) {
                    val nameWithoutExt = fileName.substringBeforeLast(".")
                    val ext = fileName.substringAfterLast(".", "")
                    val newName = if (ext.isNotEmpty()) "${nameWithoutExt}($suffix).$ext" else "${fileName}($suffix)"
                    targetFile = File(targetDir, newName)
                    suffix++
                }

                // 使用流拷贝替代 renameTo，确保跨分区保存成功
                val success = withContext(Dispatchers.IO) {
                    try {
                        tempFile.inputStream().use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        tempFile.delete()
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }

                if (success) {
                    val historyId = fileState.pendingSaveHistoryId
                    if (historyId > 0) {
                        historyRepository.updateUri(historyId, Uri.fromFile(targetFile).toString())
                    }
                    _uiEvent.emit("文件已保存: ${targetFile.absolutePath}")
                } else {
                    _uiEvent.emit("文件保存失败")
                }

                fileState = fileState.copy(
                    pendingSaveHistoryId = -1,
                    pendingSaveFileName = "",
                    pendingSaveFileSize = 0,
                    pendingSaveTempPath = ""
                )
            } catch (e: Exception) {
                _uiEvent.emit("保存失败: ${e.message}")
            }
        }
    }

    fun cancelPendingSave() {
        val tempPath = fileState.pendingSaveTempPath
        if (tempPath.isNotEmpty()) {
            File(tempPath).delete()
        }
        fileState = fileState.copy(
            pendingSaveHistoryId = -1,
            pendingSaveFileName = "",
            pendingSaveFileSize = 0,
            pendingSaveTempPath = ""
        )
    }
}
