package com.example.documentsend.network.handlers.receive

import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.handlers.INetworkListener
import java.io.DataInputStream

interface IPacketReceiver {
    suspend fun receive(header: PacketHeader, dis: DataInputStream, listener: INetworkListener, senderIp: String = ""): Result<Unit>
}
