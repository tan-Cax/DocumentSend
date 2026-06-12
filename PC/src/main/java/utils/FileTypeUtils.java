package utils;

import protocol.PacketType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileTypeUtils {

    private static final Map<String, PacketType> EXTENSION_MAP = new HashMap<>();

    // 后缀名与文件类型映射
    static {
        // 文本类型
        EXTENSION_MAP.put("txt", PacketType.TEXT);
        EXTENSION_MAP.put("log", PacketType.TEXT);
        EXTENSION_MAP.put("csv", PacketType.TEXT);
        EXTENSION_MAP.put("json", PacketType.TEXT);
        EXTENSION_MAP.put("xml", PacketType.TEXT);
        EXTENSION_MAP.put("md", PacketType.TEXT);

        // 图片类型
        EXTENSION_MAP.put("jpg", PacketType.IMAGE);
        EXTENSION_MAP.put("jpeg", PacketType.IMAGE);
        EXTENSION_MAP.put("png", PacketType.IMAGE);
        EXTENSION_MAP.put("gif", PacketType.IMAGE);
        EXTENSION_MAP.put("bmp", PacketType.IMAGE);
        EXTENSION_MAP.put("webp", PacketType.IMAGE);
        EXTENSION_MAP.put("svg", PacketType.IMAGE);
        EXTENSION_MAP.put("ico", PacketType.IMAGE);
        EXTENSION_MAP.put("heic", PacketType.IMAGE);

        // 视频类型
        EXTENSION_MAP.put("mp4", PacketType.VIDEO);
        EXTENSION_MAP.put("avi", PacketType.VIDEO);
        EXTENSION_MAP.put("mkv", PacketType.VIDEO);
        EXTENSION_MAP.put("mov", PacketType.VIDEO);
        EXTENSION_MAP.put("wmv", PacketType.VIDEO);
        EXTENSION_MAP.put("flv", PacketType.VIDEO);
        EXTENSION_MAP.put("webm", PacketType.VIDEO);
        EXTENSION_MAP.put("3gp", PacketType.VIDEO);

        // 压缩包类型
        EXTENSION_MAP.put("zip", PacketType.ARCHIVE);
        EXTENSION_MAP.put("rar", PacketType.ARCHIVE);
        EXTENSION_MAP.put("7z", PacketType.ARCHIVE);
        EXTENSION_MAP.put("tar", PacketType.ARCHIVE);
        EXTENSION_MAP.put("gz", PacketType.ARCHIVE);
        EXTENSION_MAP.put("bz2", PacketType.ARCHIVE);
        EXTENSION_MAP.put("xz", PacketType.ARCHIVE);
    }

    /**
     * 根据文件后缀名判断 PacketType
     */
    public static PacketType getTypeByFile(File file) {
        if (file == null) return PacketType.FILE;
        return getTypeByExtension(getExtension(file.getName()));
    }

    /**
     * 根据后缀名判断 PacketType
     */
    public static PacketType getTypeByExtension(String extension) {
        if (extension == null || extension.isEmpty()) return PacketType.FILE;
        return EXTENSION_MAP.getOrDefault(extension.toLowerCase(), PacketType.FILE);
    }

    /**
     * 获取文件后缀名（不含点）
     */
    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) return "";
        return fileName.substring(dotIndex + 1);
    }

    /**
     * 格式化文件大小为可读字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
