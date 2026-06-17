package com.example.documentsend.udp

import org.json.JSONObject

data class UdpAnnounce(
    val type: String = "ANNOUNCE",
    val uuid: String,
    val device: String,
    val tcpPort: Int,
    val deviceName: String = "",
    val senderIp: String = ""
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("type", type)
            put("uuid", uuid)
            put("device", device)
            put("tcpPort", tcpPort)
            put("deviceName", deviceName)
        }.toString()
    }

    companion object {
        const val UDP_PORT = 50001
        const val BROADCAST_INTERVAL_MS = 3000L
        const val DEVICE_TIMEOUT_MS = 10000L

        fun fromJson(json: String): UdpAnnounce? {
            return try {
                val obj = JSONObject(json)
                val type = obj.optString("type", "")
                if (type != "ANNOUNCE") return null
                UdpAnnounce(
                    type = type,
                    uuid = obj.getString("uuid"),
                    device = obj.getString("device"),
                    tcpPort = obj.getInt("tcpPort"),
                    deviceName = obj.optString("deviceName", "")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
