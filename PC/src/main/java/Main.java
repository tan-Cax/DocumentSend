import javafx.application.Application;
import listener.INetworkListener;
import network.SocketConnectionManager;
import ui.App;
import utils.AppConfig;
import utils.NetworkUtils;

import java.io.File;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        // 启动检测：确保 storage 目录存在
        File storageDir = AppConfig.getSaveDirFile();
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        System.out.println("用户名: " + AppConfig.getUsername());
        System.out.println("文件保存目录: " + storageDir.getAbsolutePath());
        System.out.println("监听端口: " + AppConfig.getListenPort());
        System.out.println("发送端口: " + AppConfig.getSendPort());

        // 打印本机 IP
        InetAddress localIp = NetworkUtils.getLocalIpv4Address();
        if (localIp != null) {
            System.out.println("本机 IP 地址: " + localIp.getHostAddress());
        }

        // 启动网络引擎（后台运行）
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

            @Override
            public void onFileReceived(File file) {
                System.out.println(">>> 收到文件: " + file.getName() + " (" + file.length() + " bytes)");
            }

            @Override
            public void onFileSent(File file) {
                System.out.println("<<< 文件发送完成: " + file.getName());
            }
        });

        engine.startListening(AppConfig.getListenPort());

        // 启动 JavaFX UI
        Application.launch(App.class, args);
    }
}
