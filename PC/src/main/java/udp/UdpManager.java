package udp;

import network.NetworkErrorCallback;
import utils.NetworkUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * UDP 管理器 - 纯管理器，负责 UDP Socket 的生命周期
 */
public class UdpManager {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private DatagramSocket socket;
    private Thread receiveThread;
    private volatile boolean isRunning;
    private BiConsumer<byte[], InetAddress> receiveListener;
    private int listenPort;

    public void setOnReceiveListener(BiConsumer<byte[], InetAddress> listener) {
        this.receiveListener = listener;
    }

    public void startListening(int port) {
        if (isRunning) {
            NetworkErrorCallback.getInstance().generalError("UDP 管理器已在运行中");
            return;
        }

        this.listenPort = port;
        this.isRunning = true;

        receiveThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(port);
                socket.setBroadcast(true);
                System.out.println("[UDP] Socket 已创建, 监听端口: " + port);

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

                while (isRunning) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        byte[] receivedData = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), packet.getOffset(), receivedData, 0, packet.getLength());

                        InetAddress senderAddress = packet.getAddress();
                        System.out.println("[UDP] 收到数据, 来自: " + senderAddress.getHostAddress() + ":" + packet.getPort() + ", 大小: " + receivedData.length + " 字节");

                        if (receiveListener != null) {
                            receiveListener.accept(receivedData, senderAddress);
                        }
                    } catch (SocketException e) {
                        if (isRunning) {
                            NetworkErrorCallback.getInstance().generalError("UDP 接收异常: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        NetworkErrorCallback.getInstance().generalError("UDP 接收错误: " + e.getMessage());
                    }
                }
            } catch (SocketException e) {
                NetworkErrorCallback.getInstance().generalError("UDP Socket 创建失败: " + e.getMessage());
            } finally {
                stop();
            }
        }, "UDP-Receive-Thread");

        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public void send(byte[] data, InetAddress address, int port) {
        if (socket == null || socket.isClosed()) {
            NetworkErrorCallback.getInstance().generalError("UDP Socket 未初始化或已关闭");
            return;
        }

        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            System.out.println("[UDP] 发送数据, 目标: " + address.getHostAddress() + ":" + port + ", 大小: " + data.length + " 字节");
        } catch (IOException e) {
            NetworkErrorCallback.getInstance().generalError("UDP 发送失败: " + e.getMessage());
        }
    }

    public void sendBroadcast(byte[] data, int port) {

        List<InetAddress> broadcasts =
                NetworkUtils.getBroadcastAddresses();

        for (InetAddress address : broadcasts) {

            System.out.println(
                    "[UDP] 广播发送到: "
                            + address.getHostAddress());

            send(data, address, port);
        }
    }

    public void stop() {
        isRunning = false;
        System.out.println("[UDP] 正在停止...");

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getListenPort() {
        return listenPort;
    }
}
