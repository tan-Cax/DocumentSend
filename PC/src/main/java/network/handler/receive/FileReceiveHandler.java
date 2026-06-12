package network.handler.receive;

import listener.INetworkListener;
import protocol.PacketHeader;
import utils.AppConfig;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class FileReceiveHandler implements IReceiveHandler {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public void handleReceive(PacketHeader header, DataInputStream dis, INetworkListener listener) throws IOException {
        // 1. 读取文件名
        byte[] nameBytes = new byte[header.getNameLength()];
        dis.readFully(nameBytes);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8);

        // 2. 从获取保存目录
        File saveDir = AppConfig.getSaveDirFile();

        // 3. 确定输出文件（断点续传时保持原文件名）
        File outputFile = new File(saveDir, fileName);

        // 4. 新文件（offset=0）时处理重名
        if (header.getOffset() == 0 && outputFile.exists()) {
            String baseName = fileName;
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }
            int counter = 1;
            while (outputFile.exists()) {
                outputFile = new File(saveDir, baseName + "(" + counter + ")" + extension);
                counter++;
            }
        }

        // 5. 使用 RandomAccessFile 支持断点续传
        long offset = header.getOffset();
        long bodyLength = header.getBodyLength();
        long totalLength = header.getTotalLength();

        try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
            raf.seek(offset);

            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = bodyLength;

            while (remaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = dis.read(buffer, 0, bytesToRead);
                if (bytesRead == -1) break;

                raf.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }

            // 6. 检查是否接收完成
            if (raf.length() >= totalLength) {
                System.out.println("文件接收完成: " + outputFile.getAbsolutePath());
                if (listener != null) {
                    listener.onFileReceived(outputFile);
                }
            } else {
                System.out.println("文件部分接收: " + raf.length() + "/" + totalLength + " bytes");
            }
        }
    }
}
