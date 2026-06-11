package com.example.documentsend.network.handlers.send

import com.example.documentsend.network.PacketType
import java.io.DataOutputStream


interface IPacketSender {
    suspend fun send(dos: DataOutputStream, content: SendContent, packetType: PacketType): Result<Unit>
}
