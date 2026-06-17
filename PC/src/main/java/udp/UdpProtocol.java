package udp;

import network.NetworkErrorCallback;

public class UdpProtocol {
    public static final String TYPE_ANNOUNCE = "ANNOUNCE";
    public static final int UDP_PORT = 50001;

    public static final String DEVICE_PC = "pc";
    public static final String DEVICE_ANDROID = "android";

    private final String type;
    private final String uuid;
    private final String name;
    private final String device;
    private final int tcpPort;
    private final boolean reply;

    public UdpProtocol(String type, String uuid, String name, String device, int tcpPort) {
        this(type, uuid, name, device, tcpPort, false);
    }

    public UdpProtocol(String type, String uuid, String name, String device, int tcpPort, boolean reply) {
        this.type = type;
        this.uuid = uuid;
        this.name = name;
        this.device = device;
        this.tcpPort = tcpPort;
        this.reply = reply;
    }

    public String toJson() {
        return "{" +
                "\"type\":\"" + escapeJson(type) + "\"," +
                "\"uuid\":\"" + escapeJson(uuid) + "\"," +
                "\"deviceName\":\"" + escapeJson(name) + "\"," +
                "\"device\":\"" + escapeJson(device) + "\"," +
                "\"tcpPort\":" + tcpPort + "," +
                "\"reply\":" + reply +
                "}";
    }

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
            boolean reply = extractBooleanValueSafe(json, "reply", false);

            return new UdpProtocol(type, uuid, name, device, tcpPort, reply);
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

    private static boolean extractBooleanValueSafe(String json, String key, boolean defaultValue) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) {
                return defaultValue;
            }
            startIndex += searchKey.length();

            if (json.startsWith("true", startIndex)) return true;
            if (json.startsWith("false", startIndex)) return false;
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
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

    public String getType() { return type; }
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDevice() { return device; }
    public int getTcpPort() { return tcpPort; }
    public boolean isReply() { return reply; }

    @Override
    public String toString() {
        return toJson();
    }
}
