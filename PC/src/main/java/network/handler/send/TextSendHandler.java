package network.handler.send;

import protocol.PacketHeader;
import protocol.PacketType;
import utils.CryptoUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TextSendHandler implements ISendHandler<String> {

    @Override
    public void handleSend(String text, DataOutputStream dos) throws IOException {
        if (text == null) return;

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        // 1. 组装报头
        PacketHeader header = new PacketHeader(
                PacketType.TEXT.getValue(),
                (short) 0,           // 文本无名字
                textBytes.length,    // Body 长度
                0, 0                 // 预留字段填0
        );

        // 2. 发送报头
        header.writeTo(dos);
        
        // 3. 发送报体并刷新流
        dos.write(CryptoUtils.xor(textBytes));
        dos.flush();
    }
}
