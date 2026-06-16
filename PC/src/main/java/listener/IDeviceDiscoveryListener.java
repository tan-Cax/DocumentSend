package listener;

/**
 * 设备发现监听接口
 */
public interface IDeviceDiscoveryListener {
    
    /**
     * 设备信息内部类
     */
    class DeviceInfo {
        private final String uuid;
        private final String name;
        private final String device;
        private final String ip;
        private final int tcpPort;
        private final long discoveredTime;
        
        public DeviceInfo(String uuid, String name, String device, String ip, int tcpPort) {
            this.uuid = uuid;
            this.name = name;
            this.device = device;
            this.ip = ip;
            this.tcpPort = tcpPort;
            this.discoveredTime = System.currentTimeMillis();
        }
        
        // Getters
        public String getUuid() { return uuid; }
        public String getName() { return name; }
        public String getDevice() { return device; }
        public String getIp() { return ip; }
        public int getTcpPort() { return tcpPort; }
        public long getDiscoveredTime() { return discoveredTime; }
        
        @Override
        public String toString() {
            return "DeviceInfo{" +
                    "uuid='" + uuid + '\'' +
                    ", name='" + name + '\'' +
                    ", device='" + device + '\'' +
                    ", ip='" + ip + '\'' +
                    ", tcpPort=" + tcpPort +
                    '}';
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceInfo that = (DeviceInfo) o;
            return uuid.equals(that.uuid);
        }
        
        @Override
        public int hashCode() {
            return uuid.hashCode();
        }
    }
    
    /**
     * 当发现新设备时触发
     * @param deviceInfo 设备信息
     */
    void onDeviceDiscovered(DeviceInfo deviceInfo);
    
    /**
     * 当设备离线时触发（可选实现）
     * @param uuid 设备 UUID
     */
    void onDeviceLost(String uuid);
}
