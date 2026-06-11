package com.example.documentsend.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.documentsend.data.AppDatabase
import com.example.documentsend.data.FileState
import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.manager.ReceiveManager
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.SocketClient
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.repository.Repository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.documentsend.network.PacketType
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.network.handlers.send.SendContent
import com.example.documentsend.repository.SettingsRepository
import com.example.documentsend.repository.dataStore
import com.example.documentsend.utils.FileUtils
import com.example.documentsend.utils.GetLocalIP
import com.example.documentsend.utils.HistoryUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class DocViewModel(application: Application) :
    AndroidViewModel(application) {

    private val transferManager = TransferManager.getInstance()
    private val settingsRepository = SettingsRepository(application.dataStore)
    private val historyDao = AppDatabase.getDatabase(application).historyDao()
    private val historyRepository = HistoryRepository(historyDao)
    private val ipDao = AppDatabase.getDatabase(application).ipAddressDao()
    private val repository = Repository(ipDao)
    private val _uiEvent = Channel<String>()

    private var saveToHistory = true

    private val socketSendClient = SocketClient(
        context = application,
        transferManager = transferManager,
        historyRepository = historyRepository
    )

    private val receiveManager = ReceiveManager.getInstance()

    val uiEvent = _uiEvent.receiveAsFlow()
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
    }

    fun startServer(port: Int) {
        receiveManager.startServer(port, object : INetworkListener {
            override fun onConnected(clientIp: String) {
                viewModelScope.launch { _uiEvent.send("客户端已连接: $clientIp") }
            }
            override fun onDisconnected() {
                viewModelScope.launch { _uiEvent.send("客户端已断开") }
            }
            override fun onTextMessage(text: String) {
                viewModelScope.launch { _uiEvent.send("收到文本: $text") }
            }
            override fun onFileStarted(fileName: String, totalLength: Long) {
                viewModelScope.launch { _uiEvent.send("开始接收文件: $fileName") }
            }
            override fun onFileProgress(fileName: String, currentLength: Long, totalLength: Long) {
            }
            override fun onFileFinished(fileName: String) {
                viewModelScope.launch { _uiEvent.send("文件接收完成: $fileName") }
            }
            override fun onError(message: String) {
                viewModelScope.launch { _uiEvent.send("错误: $message") }
            }
        })
    }

    fun stopServer() {
        receiveManager.stopServer()
    }

    fun sendText() {
        val type = fileState.packetType ?: return
        viewModelScope.launch {
            val history = HistoryUtils.createTextSendRecoder(
                content = fileState.inputMessage ?: "",
                targetIp = fileState.inputIp ?: "",
            )
            historyRepository.insertHistory(history)

            val content = SendContent.fromText(fileState.inputMessage)
            val result = socketSendClient.send(fileState.inputIp, fileState.port, type, content)
            result.onSuccess {
                _uiEvent.send("发送成功")
            }.onFailure { e ->
                _uiEvent.send("发送失败: ${e.message}")
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
            historyRepository.insertHistory(history)

            val content = SendContent.fromUri(
                getApplication<Application>(),
                uri,
                offset = 0,
                historyId = history.id
            )
            if (content == null) {
                _uiEvent.send("无法获取文件内容")
                return@launch
            }

            val result = socketSendClient.send(fileState.inputIp, fileState.port, type, content)
            result.onSuccess {
                _uiEvent.send("发送成功")
            }.onFailure { e ->
                _uiEvent.send("发送失败: ${e.message}")
            }
        }
    }

    fun sendFromBreakpoint(history: History) {
        val uri = HistoryUtils.getUri(history) ?: return
        val type = HistoryUtils.getPacketType(history) ?: return

        viewModelScope.launch {
            val content = SendContent.fromUri(
                getApplication<Application>(),
                uri,
                offset = history.offset,
                historyId = history.id
            )
            if (content == null) {
                _uiEvent.send("无法获取文件内容")
                return@launch
            }

            val result = socketSendClient.send(history.targetIp, fileState.port, type, content)
            result.onSuccess {
                _uiEvent.send("续传成功")
            }.onFailure { e ->
                _uiEvent.send("续传失败: ${e.message}")
            }
        }
    }

    fun updateInputIp(ip: String) {
        fileState = fileState.copy(inputIp = ip)
    }

    fun updateInputMessage(message: String) {
        fileState = fileState.copy(inputMessage = message)
    }

    fun updateTransferType(packetType: PacketType?) {
        fileState = fileState.copy(packetType = packetType)
    }

    fun updateSelectedUri(uri: android.net.Uri?) {
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
                _uiEvent.send("未获取到IP地址")
            }
        }
    }
}
