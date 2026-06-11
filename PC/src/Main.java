import listener.INetworkListener;
import network.SocketConnectionManager;
import utils.NetworkUtils;

import java.net.InetAddress;

public class Main {
    private static final int PORT = 6666;

    public static void main(String[] args) {
        // 打印本机 IP
        InetAddress localIp = NetworkUtils.getLocalIpv4Address();
        if (localIp != null) {
            System.out.println("本机 IP 地址: " + localIp.getHostAddress());
        }

        // 1. 初始化网络引擎并传入监听器
        SocketConnectionManager engine = new SocketConnectionManager(new INetworkListener() {
            @Override
            public void onConnected(String clientIp) {
                System.out.println(">>> 客户端上线: " + clientIp);
            }

            @Override
            public void onDisconnected() {
                System.out.println(">>> 客户端已断开连接");
            }

            @Override
            public void onTextMessage(String text) {
                System.out.println(">>> 收到文本消息: " + text);
            }
        });

        // 2. 启动监听
        engine.startListening(PORT);

        // 主线程阻塞，保持程序运行
        // 未来可以在这里使用 engine.getWriter().sendText("...") 来主动发消息
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
