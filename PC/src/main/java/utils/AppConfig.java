package utils;

import java.io.File;
import java.util.UUID;

public class AppConfig {
    private static final String DEFAULT_SAVE_DIR = System.getProperty("user.dir") + File.separator + "storage";
    private static final int DEFAULT_LISTEN_PORT = 50000;
    private static final int DEFAULT_SEND_PORT = 50001;
    private static final String DEFAULT_USERNAME = "User";
    private static final String DEFAULT_TARGET_IP = "127.0.0.1";

    private static ConfigManager configManager;
    private static String saveDir;
    private static int listenPort;
    private static int sendPort;
    private static String username;
    private static String targetIp;
    private static String uuid;

    static {
        saveDir = DEFAULT_SAVE_DIR;
        listenPort = DEFAULT_LISTEN_PORT;
        sendPort = DEFAULT_SEND_PORT;
        username = DEFAULT_USERNAME;
        targetIp = DEFAULT_TARGET_IP;

        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        configManager = new ConfigManager(saveDir);
        load();
    }

    private static void load() {
        saveDir = configManager.getString("saveDir", DEFAULT_SAVE_DIR);
        listenPort = configManager.getInt("listenPort", DEFAULT_LISTEN_PORT);
        sendPort = configManager.getInt("sendPort", DEFAULT_SEND_PORT);
        username = configManager.getString("username", DEFAULT_USERNAME);
        targetIp = configManager.getString("targetIp", DEFAULT_TARGET_IP);
        
        // 加载或生成 UUID
        uuid = configManager.getString("uuid", "");
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            configManager.set("uuid", uuid);
            System.out.println("生成新的设备 UUID: " + uuid);
        } else {
            System.out.println("加载设备 UUID: " + uuid);
        }

        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // --- SaveDir ---
    public static String getSaveDir() { return saveDir; }
    public static void setSaveDir(String path) {
        saveDir = path;
        configManager.set("saveDir", path);
        File dir = new File(saveDir);
        if (!dir.exists()) dir.mkdirs();
    }
    public static File getSaveDirFile() { return new File(saveDir); }

    // --- ListenPort ---
    public static int getListenPort() { return listenPort; }
    public static void setListenPort(int port) {
        listenPort = port;
        configManager.setInt("listenPort", port);
    }

    // --- SendPort ---
    public static int getSendPort() { return sendPort; }
    public static void setSendPort(int port) {
        sendPort = port;
        configManager.setInt("sendPort", port);
    }

    // --- Username ---
    public static String getUsername() { return username; }
    public static void setUsername(String name) {
        username = name;
        configManager.set("username", name);
    }

    // --- TargetIp ---
    public static String getTargetIp() { return targetIp; }
    public static void setTargetIp(String ip) {
        targetIp = ip;
        configManager.set("targetIp", ip);
    }
    
    // --- UUID ---
    public static String getUuid() { return uuid; }
}
