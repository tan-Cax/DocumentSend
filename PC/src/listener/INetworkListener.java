package listener;

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

    // TODO: 后续添加接收文件、进度更新等回调方法
}
