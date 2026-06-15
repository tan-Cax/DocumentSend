package network.handler.receive;

import network.NetworkErrorCallback;
import listener.INetworkListener;
import protocol.PacketHeader;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class  TextReceiveHandler implements IReceiveHandler {

    @Override
    public void handleReceive(PacketHeader header, DataInputStream dis, INetworkListener listener) throws IOException {
        // 1. 读取名称 (文本类型通常没有名称，若有则跳过)
        if (header.getNameLength() > 0) {
            byte[] nameBytes = new byte[header.getNameLength()];
            dis.readFully(nameBytes);
        }

        // 2. 读取文本内容
        long bodyLength = header.getBodyLength();
        if (bodyLength > Integer.MAX_VALUE) {
            NetworkErrorCallback.getInstance().textError("文本消息过长，拒绝接收，已跳过数据流");
            skipBytesFully(dis, bodyLength);
            return;
        }

        byte[] bodyBytes = new byte[(int) bodyLength];
        dis.readFully(bodyBytes);
        String text = new String(bodyBytes, StandardCharsets.UTF_8);

        // 3. 触发回调
        if (listener != null) {
            listener.onTextMessage(text);
        }
    }

    private void skipBytesFully(DataInputStream dis, long bytesToSkip) throws IOException {
        long remaining = bytesToSkip;
        while (remaining > 0) {
            long skipped = dis.skip(remaining);
            if (skipped <= 0) {
                break;
            }
            remaining -= skipped;
        }
    }
}
