package listener;

import java.io.File;

public interface INetworkListener {
    /**
     * 当客户端成功连接时触发
     * @param clientIp 客户端IP地址
     */
    void onConnected(String clientIp);

    /**
     * 当客户端断开连接时触发
     */
    void onDisconnected();

    /**
     * 收到文本消息时触发
     * @param text 接收到的文本内容
     */
    void onTextMessage(String text);

    /**
     * 收到文件时触发
     * @param file 接收到的本地文件
     */
    void onFileReceived(File file);

    /**
     * 文件发送完成时触发
     * @param file 已发送的文件
     */
    void onFileSent(File file);
}
