package udp;

import network.NetworkErrorCallback;

/**
 * UDP 协议定义
 * 协议格式：
 * {
 *     "type": "ANNOUNCE",
 *     "uuid": "xxxxxxxx",
 *     "deviceName": "我的设备",
 *     "device": "pc",
 *     "tcpPort": 6666
 * }
 */
public class UdpProtocol {
    // 协议类型常量
    public static final String TYPE_ANNOUNCE = "ANNOUNCE";

    // 设备类型常量
    public static final String DEVICE_PC = "pc";
    public static final String DEVICE_ANDROID = "android";

    private final String type;
    private final String uuid;
    private final String name;
    private final String device;
    private final int tcpPort;

    public UdpProtocol(String type, String uuid, String name, String device, int tcpPort) {
        this.type = type;
        this.uuid = uuid;
        this.name = name;
        this.device = device;
        this.tcpPort = tcpPort;
    }

    /**
     * 序列化为 JSON 字符串
     */
    public String toJson() {
        return "{" +
                "\"type\":\"" + escapeJson(type) + "\"," +
                "\"uuid\":\"" + escapeJson(uuid) + "\"," +
                "\"deviceName\":\"" + escapeJson(name) + "\"," +
                "\"device\":\"" + escapeJson(device) + "\"," +
                "\"tcpPort\":" + tcpPort +
                "}";
    }

    /**
     * 从 JSON 字符串反序列化
     */
    public static UdpProtocol fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            json = json.trim();

            String type = extractStringValue(json, "type");
            String uuid = extractStringValue(json, "uuid");
            String name = extractStringValueSafe(json, "deviceName", "");
            String device = extractStringValue(json, "device");
            int tcpPort = extractIntValue(json, "tcpPort");

            return new UdpProtocol(type, uuid, name, device, tcpPort);
        } catch (Exception e) {
            NetworkErrorCallback.getInstance().generalError("UDP JSON 解析失败: " + e.getMessage());
            return null;
        }
    }

    private static String extractStringValueSafe(String json, String key, String defaultValue) {
        try {
            return extractStringValue(json, key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String extractStringValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        startIndex += searchKey.length();

        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            throw new IllegalArgumentException("Invalid JSON format for key: " + key);
        }

        return unescapeJson(json.substring(startIndex, endIndex));
    }

    private static int extractIntValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        startIndex += searchKey.length();

        int endIndex = startIndex;
        while (endIndex < json.length() &&
               (Character.isDigit(json.charAt(endIndex)) || json.charAt(endIndex) == '-')) {
            endIndex++;
        }

        if (endIndex == startIndex) {
            throw new IllegalArgumentException("Invalid JSON format for key: " + key);
        }

        return Integer.parseInt(json.substring(startIndex, endIndex));
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private static String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t")
                  .replace("\\\\", "\\");
    }

    // Getters
    public String getType() { return type; }
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDevice() { return device; }
    public int getTcpPort() { return tcpPort; }

    @Override
    public String toString() {
        return toJson();
    }
}
