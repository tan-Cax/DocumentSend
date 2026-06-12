package utils;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private final File configFile;
    private final Properties properties;

    public ConfigManager(String dir) {
        this.configFile = new File(dir, CONFIG_FILE);
        this.properties = new Properties();
        load();
    }

    public void load() {
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("加载配置文件失败: " + e.getMessage());
            }
        }
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            properties.store(fos, "DocumentSend Config");
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return defaultValue;
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    public void setInt(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
        save();
    }
}
