package com.example.documentsend.network.handlers

import com.example.documentsend.data.History

interface INetworkListener {
    fun onConnected(clientIp: String)
    fun onDisconnected()
    fun onTextMessage(text: String)
    fun onFileStarted(fileName: String, totalLength: Long)
    fun onFileProgress(fileName: String, currentLength: Long, totalLength: Long)
    fun onFileFinished(fileName: String)
    fun onFileReadyToSave(historyId: Int, fileName: String, fileSize: Long, tempPath: String)
    fun onError(message: String)
    fun onReceiveRecord(record: History)
}
