package network;

import network.handler.send.ISendHandler;
import network.handler.send.TextSendHandler;
import protocol.PacketType;

import java.io.DataOutputStream;
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
        // 注册发送处理器
        sendHandlers.put(PacketType.TEXT, new TextSendHandler());
    }

    public void attach(Socket socket) throws IOException {
        if (socket != null && !socket.isClosed()) {
            this.dos = new DataOutputStream(socket.getOutputStream());
        }
    }

    /**
     * 发送文本消息
     */
    @SuppressWarnings("unchecked")
    public void sendText(String text) {
        if (dos == null) {
            System.err.println("发送端未绑定到有效的连接");
            return;
        }

        new Thread(() -> {
            try {
                ISendHandler<String> handler = (ISendHandler<String>) sendHandlers.get(PacketType.TEXT);
                if (handler != null) {
                    handler.handleSend(text, dos);
                }
            } catch (IOException e) {
                System.err.println("发送文本消息失败: " + e.getMessage());
            }
        }).start();
    }
}
