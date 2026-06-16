package udp;

import listener.IDeviceDiscoveryListener;
import listener.IDeviceDiscoveryListener.DeviceInfo;
import network.NetworkErrorCallback;

import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UDP 业务服务 - 负责设备发现和设备列表维护
 */
public class UdpService {
    private final UdpManager udpManager;
    private final ConcurrentHashMap<String, DeviceInfo> discoveredDevices;
    private IDeviceDiscoveryListener deviceListener;
    private final String localUuid;
    private final String localName;
    private final int localTcpPort;

    public UdpService(String localUuid, String localName, int localTcpPort) {
        this.localUuid = localUuid;
        this.localName = localName;
        this.localTcpPort = localTcpPort;
        this.udpManager = new UdpManager();
        this.discoveredDevices = new ConcurrentHashMap<>();

        udpManager.setOnReceiveListener(this::handleReceive);
    }

    public void setDeviceDiscoveryListener(IDeviceDiscoveryListener listener) {
        this.deviceListener = listener;
    }

    public void startListening(int port) {
        udpManager.startListening(port);
    }

    public void sendBroadcast() {
        UdpProtocol protocol = new UdpProtocol(
                UdpProtocol.TYPE_ANNOUNCE,
                localUuid,
                localName,
                UdpProtocol.DEVICE_PC,
                localTcpPort
        );

        String json = protocol.toJson();
        byte[] data = json.getBytes();

        int port = udpManager.getListenPort();
        if (port <= 0) {
            port = 9999;
        }

        udpManager.sendBroadcast(data, port);
    }

    private void handleReceive(byte[] data, InetAddress senderAddress) {
        try {
            String json = new String(data);

            UdpProtocol protocol = UdpProtocol.fromJson(json);
            if (protocol == null) {
                NetworkErrorCallback.getInstance().generalError("UDP 协议解析失败");
                return;
            }

            if (protocol.getUuid().equals(localUuid)) {
                return;
            }

            if (!UdpProtocol.TYPE_ANNOUNCE.equals(protocol.getType())) {
                NetworkErrorCallback.getInstance().generalError("未知的 UDP 协议类型: " + protocol.getType());
                return;
            }

            DeviceInfo deviceInfo = new DeviceInfo(
                    protocol.getUuid(),
                    protocol.getName(),
                    protocol.getDevice(),
                    senderAddress.getHostAddress(),
                    protocol.getTcpPort()
            );

            boolean isNewDevice = !discoveredDevices.containsKey(protocol.getUuid());

            discoveredDevices.put(protocol.getUuid(), deviceInfo);

            if (isNewDevice && deviceListener != null) {
                deviceListener.onDeviceDiscovered(deviceInfo);
            }

        } catch (Exception e) {
            NetworkErrorCallback.getInstance().generalError("处理 UDP 数据异常: " + e.getMessage());
        }
    }

    public Collection<DeviceInfo> getDiscoveredDevices() {
        return discoveredDevices.values();
    }

    public DeviceInfo getDevice(String uuid) {
        return discoveredDevices.get(uuid);
    }

    public void removeDevice(String uuid) {
        DeviceInfo removed = discoveredDevices.remove(uuid);
        if (removed != null && deviceListener != null) {
            deviceListener.onDeviceLost(uuid);
        }
    }

    public void clearDevices() {
        discoveredDevices.clear();
    }

    public void stop() {
        udpManager.stop();
        discoveredDevices.clear();
    }

    public boolean isRunning() {
        return udpManager.isRunning();
    }
}
