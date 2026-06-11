package com.example.documentsend.manager

import android.content.Context
import com.example.documentsend.network.SocketServer
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.repository.HistoryRepository

//接收端管理器，负责管理接收端的网络服务和数据存储
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

    fun init(
        context: Context,
        transferManager: TransferManager,
        historyRepository: HistoryRepository
    ) {
        socketServer = SocketServer(
            context = context,
            transferManager = transferManager,
            historyRepository = historyRepository
        )
    }

    //启动接收服务
    fun startServer(port: Int, listener: INetworkListener) {
        socketServer?.start(port, listener)
    }

    //停止接收服务
    fun stopServer() {
        socketServer?.stop()
    }
}
