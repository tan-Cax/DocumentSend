package com.example.documentsend.network.handlers

interface INetworkListener {
    fun onConnected(clientIp: String)
    fun onDisconnected()
    fun onTextMessage(text: String)
    fun onFileStarted(fileName: String, totalLength: Long)
    fun onFileProgress(fileName: String, currentLength: Long, totalLength: Long)
    fun onFileFinished(fileName: String)
    fun onError(message: String)
}
