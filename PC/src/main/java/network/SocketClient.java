package network;

import network.handler.send.FileSendHandler;
import network.handler.send.ISendHandler;
import network.handler.send.TextSendHandler;
import protocol.PacketType;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * SocketClient 作为纯粹的发送器路由
 */
public class SocketClient {
    private DataOutputStream dos;
    private final Map<PacketType, ISendHandler<?>> sendHandlers = new HashMap<>();

    public SocketClient() {
        sendHandlers.put(PacketType.TEXT, new TextSendHandler());
        FileSendHandler fileHandler = new FileSendHandler();
        sendHandlers.put(PacketType.FILE, fileHandler);
        sendHandlers.put(PacketType.IMAGE, fileHandler);
        sendHandlers.put(PacketType.VIDEO, fileHandler);
        sendHandlers.put(PacketType.ARCHIVE, fileHandler);
    }

    /**
     * 绑定已有连接（被动连接时使用）
     */
    public void attach(Socket socket) throws IOException {
        if (socket != null && !socket.isClosed()) {
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
    }

    /**
     * 通过已有连接发送文本
     */
    @SuppressWarnings("unchecked")
    public void sendText(String text) {
        if (dos == null) {
            NetworkErrorCallback.getInstance().sendError("发送端未绑定到有效的连接");
            return;
        }
        new Thread(() -> {
            try {
                ISendHandler<String> handler = (ISendHandler<String>) sendHandlers.get(PacketType.TEXT);
                if (handler != null) {
                    handler.handleSend(text, dos);
                }
            } catch (IOException e) {
                NetworkErrorCallback.getInstance().sendError("发送文本消息失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 通过已有连接发送文件
     */
    @SuppressWarnings("unchecked")
    public void sendFile(File file) {
        if (dos == null) {
            NetworkErrorCallback.getInstance().sendError("发送端未绑定到有效的连接");
            return;
        }
        new Thread(() -> {
            try {
                ISendHandler<File> handler = (ISendHandler<File>) sendHandlers.get(PacketType.FILE);
                if (handler != null) {
                    handler.handleSend(file, dos);
                }
            } catch (IOException e) {
                NetworkErrorCallback.getInstance().sendError("发送文件失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 主动连接目标并发送文本
     */
    @SuppressWarnings("unchecked")
    public void sendTextTo(String text, String targetIp, int targetPort) {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(targetIp, targetPort);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                ISendHandler<String> handler = (ISendHandler<String>) sendHandlers.get(PacketType.TEXT);
                if (handler != null) {
                    handler.handleSend(text, out);
                }
                System.out.println("文本发送成功: " + targetIp + ":" + targetPort);
            } catch (IOException e) {
                NetworkErrorCallback.getInstance().sendError("发送文本失败: " + e.getMessage());
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try { socket.close(); } catch (IOException e) { /* ignore */ }
                }
            }
        }).start();
    }

    /**
     * 主动连接目标并发送文件
     */
    @SuppressWarnings("unchecked")
    public void sendFileTo(File file, String targetIp, int targetPort) {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(targetIp, targetPort);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                ISendHandler<File> handler = (ISendHandler<File>) sendHandlers.get(PacketType.FILE);
                if (handler != null) {
                    handler.handleSend(file, out);
                }
                System.out.println("文件发送成功: " + file.getName() + " → " + targetIp + ":" + targetPort);
            } catch (IOException e) {
                NetworkErrorCallback.getInstance().sendError("发送文件失败: " + e.getMessage());
            } finally {
                if (socket != null && !socket.isClosed()) {
                    try { socket.close(); } catch (IOException e) { /* ignore */ }
                }
            }
        }).start();
    }
}
