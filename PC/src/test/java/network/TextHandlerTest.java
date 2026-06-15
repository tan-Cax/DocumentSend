package network;

import listener.INetworkListener;
import network.handler.receive.TextReceiveHandler;
import network.handler.send.TextSendHandler;
import org.junit.jupiter.api.Test;
import protocol.PacketHeader;
import protocol.PacketType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TextHandlerTest {

    @Test
    void sendAndReceiveShouldRoundtripSimpleText() throws IOException {
        String message = "Hello, World!";
        assertRoundtrip(message);
    }

    @Test
    void sendAndReceiveShouldHandleChinese() throws IOException {
        String message = "你好，世界！文件传输测试。";
        assertRoundtrip(message);
    }

    @Test
    void sendAndReceiveShouldHandleEmptyString() throws IOException {
        String message = "";
        assertRoundtrip(message);
    }

    @Test
    void sendAndReceiveShouldHandleSpecialCharacters() throws IOException {
        String message = "Line1\nLine2\tTab\r\nSpecial: !@#$%^&*()_+{}:\"<>?";
        assertRoundtrip(message);
    }

    private void assertRoundtrip(String message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        TextSendHandler sender = new TextSendHandler();
        sender.handleSend(message, dos);

        byte[] wire = baos.toByteArray();
        assertTrue(wire.length >= 29, "Should contain header (and optional body)");

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(wire));
        PacketHeader header = PacketHeader.readFrom(dis);

        assertEquals(PacketType.TEXT.getValue(), header.getType());
        assertEquals(0, header.getNameLength());
        byte[] expectedBody = message.getBytes(StandardCharsets.UTF_8);
        assertEquals(expectedBody.length, header.getBodyLength());

        AtomicReference<String> captured = new AtomicReference<>();
        INetworkListener listener = new INetworkListener() {
            public void onConnected(String ip) {}
            public void onDisconnected() {}
            public void onTextMessage(String text) { captured.set(text); }
            public void onFileReceived(File f) {}
            public void onFileSent(File f) {}
        };

        TextReceiveHandler receiver = new TextReceiveHandler();
        receiver.handleReceive(header, dis, listener);

        assertEquals(message, captured.get());
    }

    @Test
    void receiveHandlerShouldSkipNameBytesIfPresent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(0xCAFE);
        dos.writeByte(PacketType.TEXT.getValue());
        dos.writeShort(4);
        dos.writeLong(5);
        dos.writeLong(0);
        dos.writeLong(5);
        dos.write("name".getBytes(StandardCharsets.UTF_8));
        dos.write("hello".getBytes(StandardCharsets.UTF_8));

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        PacketHeader header = PacketHeader.readFrom(dis);

        assertEquals(4, header.getNameLength());
        assertEquals(5, header.getBodyLength());

        AtomicReference<String> captured = new AtomicReference<>();
        INetworkListener listener = new INetworkListener() {
            public void onConnected(String ip) {}
            public void onDisconnected() {}
            public void onTextMessage(String text) { captured.set(text); }
            public void onFileReceived(File f) {}
            public void onFileSent(File f) {}
        };

        TextReceiveHandler receiver = new TextReceiveHandler();
        receiver.handleReceive(header, dis, listener);

        assertEquals("hello", captured.get());
    }
}
