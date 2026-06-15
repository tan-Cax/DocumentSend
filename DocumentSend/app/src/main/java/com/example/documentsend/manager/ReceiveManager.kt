package com.example.documentsend.manager

import android.content.Context
import com.example.documentsend.network.SocketServer
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.repository.HistoryRepository

class ReceiveManager private constructor() {

    private var socketServer: SocketServer? = null

    companion object {
        @Volatile
        private var INSTANCE: ReceiveManager? = null

        fun getInstance(): ReceiveManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ReceiveManager()
                INSTANCE = instance
                instance
            }
        }
    }

    private var lastListener: INetworkListener? = null
    private var lastPort: Int = 0

    fun init(
        context: Context,
        transferManager: TransferManager,
        historyRepository: HistoryRepository,
        autoSave: Boolean = true
    ) {
        socketServer?.stop()
        socketServer = SocketServer(
            context = context,
            transferManager = transferManager,
            historyRepository = historyRepository,
            autoSave = autoSave
        )
    }

    fun startServer(port: Int, listener: INetworkListener) {
        lastPort = port
        lastListener = listener
        socketServer?.start(port, listener)
    }

    fun restartServer() {
        val listener = lastListener
        val port = lastPort
        if (listener != null && port > 0) {
            socketServer?.start(port, listener)
        }
    }

    fun stopServer() {
        socketServer?.stop()
    }
}
