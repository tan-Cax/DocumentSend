package network;

import listener.INetworkListener;
import network.handler.receive.FileReceiveHandler;
import network.handler.receive.IReceiveHandler;
import network.handler.receive.TextReceiveHandler;
import protocol.PacketHeader;
import protocol.PacketType;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * SocketServer 作为纯粹的接收器路由
 */
public class SocketServer {
    private boolean isReading;
    private final Map<PacketType, IReceiveHandler> receiveHandlers = new HashMap<>();

    public SocketServer() {
        // 注册接收处理器
        receiveHandlers.put(PacketType.TEXT, new TextReceiveHandler());
        receiveHandlers.put(PacketType.FILE, new FileReceiveHandler());
        receiveHandlers.put(PacketType.IMAGE, new FileReceiveHandler());
        receiveHandlers.put(PacketType.VIDEO, new FileReceiveHandler());
        receiveHandlers.put(PacketType.ARCHIVE, new FileReceiveHandler());
    }

    public void startReading(Socket socket, INetworkListener listener) {
        if (socket == null || socket.isClosed()) return;
        
        isReading = true;
        new Thread(() -> {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                while (isReading) {
                    // 1. 读取并解析包头
                    PacketHeader header;
                    try {
                        header = PacketHeader.readFrom(dis);
                    } catch (IOException e) {
                        System.err.println("解析包头失败或断开连接: " + e.getMessage());
                        break;
                    }

                    PacketType type = PacketType.fromValue(header.getType());
                    if (type == null) {
                        System.err.println("未知的消息类型: " + header.getType() + "，跳过该包");
                        skipBytesFully(dis, header.getNameLength() + header.getBodyLength());
                        continue;
                    }

                    // 2. 查找对应的接收处理器并路由
                    IReceiveHandler handler = receiveHandlers.get(type);
                    if (handler != null) {
                        handler.handleReceive(header, dis, listener);
                    } else {
                        System.out.println("收到类型为 " + type.name() + " 的数据，暂无对应接收处理器，跳过...");
                        skipBytesFully(dis, header.getNameLength() + header.getBodyLength());
                    }
                }
            } catch (IOException e) {
                System.out.println("服务端读取异常或连接已断开: " + e.getMessage());
            } finally {
                isReading = false;
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        }).start();
    }

    public void stopReading() {
        isReading = false;
    }

    private void skipBytesFully(DataInputStream dis, long bytesToSkip) throws IOException {
        long remaining = bytesToSkip;
        while (remaining > 0) {
            long skipped = dis.skip(remaining);
            if (skipped <= 0) break;
            remaining -= skipped;
        }
    }
}
