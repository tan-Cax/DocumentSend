package network.handler.send;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ISendHandler<T> {
    /**
     * 处理特定类型的数据发送
     * @param data 要发送的数据对象
     * @param dos  网络输出流
     * @throws IOException 如果网络写入失败
     */
    void handleSend(T data, DataOutputStream dos) throws IOException;
}
