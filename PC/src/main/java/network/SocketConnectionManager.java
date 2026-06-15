package network;

import listener.INetworkListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 负责底层 Socket 通道的建立与生命周期管理
 */
public class SocketConnectionManager {
    private ServerSocket serverSocket;
    private Socket activeSocket; // 当前唯一活跃的连接
    private boolean isRunning;

    private final SocketServer reader; // 专门负责读的模块 (接收器)
    private final SocketClient writer; // 专门负责写的模块 (发送器)
    private final INetworkListener listener;

    public SocketConnectionManager(INetworkListener listener) {
        this.listener = listener;
        this.reader = new SocketServer();
        this.writer = new SocketClient();
    }

    public void startListening(int port) {
        if (isRunning) return;
        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("底层网络引擎启动成功，监听端口: " + port);

                while (isRunning) {
                    Socket newSocket = serverSocket.accept();
                    System.out.println("收到连接请求: " + newSocket.getInetAddress());

                    // 一对一连接控制：保护首个连接，拒绝后来者
                    if (activeSocket != null && !activeSocket.isClosed()) {
                        System.out.println("已有活动连接，拒绝新连接请求");
                        newSocket.close();
                        continue;
                    }

                    activeSocket = newSocket;

                    // 绑定读写模块
                    try {
                        writer.attach(activeSocket);
                        reader.startReading(activeSocket, listener);
                        
                        if (listener != null) {
                            listener.onConnected(activeSocket.getInetAddress().getHostAddress());
                        }
                    } catch (IOException e) {
                        NetworkErrorCallback.getInstance().receiveError("绑定读写模块失败: " + e.getMessage());
                        closeActiveSocket();
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    NetworkErrorCallback.getInstance().receiveError("监听端口异常: " + e.getMessage());
                } else {
                    System.out.println("网络引擎已关闭。");
                }
            }
        }).start();
    }

    public SocketClient getWriter() {
        return writer;
    }

    private void closeActiveSocket() {
        if (activeSocket != null && !activeSocket.isClosed()) {
            try {
                activeSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        isRunning = false;
        reader.stopReading();
        closeActiveSocket();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
