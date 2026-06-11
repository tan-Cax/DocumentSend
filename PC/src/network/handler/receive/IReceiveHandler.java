package network.handler.receive;

import listener.INetworkListener;
import protocol.PacketHeader;

import java.io.DataInputStream;
import java.io.IOException;

public interface IReceiveHandler {
    /**
     * 处理特定类型的数据包接收
     * @param header 解析好的报头信息
     * @param dis    网络输入流
     * @param listener 回调接口，用于通知上层
     * @throws IOException 如果网络读取中断或处理失败
     */
    void handleReceive(PacketHeader header, DataInputStream dis, INetworkListener listener) throws IOException;
}
