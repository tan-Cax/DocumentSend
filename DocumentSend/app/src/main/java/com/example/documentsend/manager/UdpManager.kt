package com.example.documentsend.manager

import com.example.documentsend.log.Logger
import com.example.documentsend.udp.UdpAnnounce
import com.example.documentsend.udp.UdpReceiver
import com.example.documentsend.udp.UdpSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiscoveredDevice(
    val uuid: String,
    val device: String,
    val deviceName: String,
    val ip: String,
    val receivePort: Int,
    val lastSeen: Long = System.currentTimeMillis()
)

class UdpManager private constructor() {

    private var sender: UdpSender? = null
    private var receiver: UdpReceiver? = null
    private var scope: CoroutineScope? = null

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: UdpManager? = null

        fun getInstance(): UdpManager {
            return INSTANCE ?: synchronized(this) {
                val instance = UdpManager()
                INSTANCE = instance
                instance
            }
        }
    }

    fun start(uuid: String, receivePort: Int, deviceName: String) {
        if (scope != null) return
        Logger.logInfo("Manager", "UdpStart", "UDP发现启动, uuid=$uuid, port=$receivePort, name=$deviceName")
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        receiver = UdpReceiver().also { it.start() }
        sender = UdpSender(uuid, receivePort, deviceName).also { it.start() }

        scope!!.launch {
            receiver!!.received.collect { announce ->
                val now = System.currentTimeMillis()
                val current = _discoveredDevices.value.toMutableList()
                val index = current.indexOfFirst { it.uuid == announce.uuid }
                if (index >= 0) {
                    current[index] = DiscoveredDevice(
                        uuid = announce.uuid,
                        device = announce.device,
                        deviceName = announce.deviceName,
                        ip = announce.senderIp,
                        receivePort = announce.receivePort,
                        lastSeen = now
                    )
                } else {
                    current.add(
                        DiscoveredDevice(
                            uuid = announce.uuid,
                            device = announce.device,
                            deviceName = announce.deviceName,
                            ip = announce.senderIp,
                            receivePort = announce.receivePort,
                            lastSeen = now
                        )
                    )
                }
                _discoveredDevices.value = current
            }
        }

        scope!!.launch {
            while (true) {
                delay(2000)
                val now = System.currentTimeMillis()
                val filtered = _discoveredDevices.value.filter {
                    now - it.lastSeen < UdpAnnounce.DEVICE_TIMEOUT_MS
                }
                if (filtered.size != _discoveredDevices.value.size) {
                    _discoveredDevices.value = filtered
                }
            }
        }
    }

    fun refreshDevices() {
        sender?.forceBroadcast()
    }

    fun stop() {
        Logger.logInfo("Manager", "UdpStop", "UDP发现停止")
        sender?.stop()
        receiver?.stop()
        scope?.cancel()
        scope = null
        sender = null
        receiver = null
        _discoveredDevices.value = emptyList()
    }
}
